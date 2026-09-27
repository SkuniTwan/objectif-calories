package com.legoueix.objectifcalories.voice

import com.legoueix.objectifcalories.ciqual.AlimentsCourants

/**
 * Découpe une phrase dictée en paires (nom, grammes). Trois formes sont acceptées, y compris
 * mélangées dans une même phrase, chaque aliment étant séparé du suivant par « et » ou une
 * virgule :
 * - « riz 150 grammes » : la reconnaissance vocale Android convertit les nombres dictés en
 *   chiffres, donc un nombre APRÈS des mots déjà accumulés termine le nom de l'aliment qui le
 *   précède et vaut pour un poids en grammes.
 * - « 2 œufs » : un nombre EN TÊTE, avant tout mot de l'aliment, vaut pour une quantité — le
 *   grammage est alors estimé (voir [EstimationGrammage]) puis multiplié par cette quantité.
 * - « un œuf et une tranche de jambon » : aucun poids ni quantité cités — le grammage de
 *   chaque aliment est estimé.
 *
 * Dans tous les cas, le nom retourné passe par [AlimentsCourants] : la recherche floue
 * générale de Ciqual (LIKE + libellé le plus court) se trompe trop souvent sur les aliments
 * les plus usuels ("riz" matche "Chorizo", "pain" matche systématiquement "Pain pita").
 */
object DictationParser {

    private val motsIgnores = setOf(
        "grammes", "gramme", "grame", "g", "environ",
        "de", "d'", "un", "une", "des", "du", "le", "la", "les",
        "au", "aux", "à", "avec",
    )
    // « et » sépare deux aliments dans la même phrase ; la virgule aussi (chaque item d'une
    // liste dictée doit rester indépendant même si un item plus tôt avait un poids explicite
    // et un item plus tard n'en a pas — sans ce découpage, le second est perdu).
    private val motsSeparateurs = setOf("et", ",")

    // Un mot de contenant/portion en tête d'un aliment sans poids ("tranche de jambon",
    // "verre de lait") sert à estimer le grammage (voir EstimationGrammage) et, pour le pain,
    // à choisir la bonne variété (voir AlimentsCourants) — mais ne fait pas partie du nom de
    // l'aliment transmis à la recherche Ciqual : Ciqual ne connaît pas "tranche jambon",
    // seulement "jambon".
    // "salade" suit le même schéma ("salade de tomates" → l'aliment cherché est "tomates",
    // pas la chaîne composée "salade tomates" qui ne correspond à rien dans Ciqual) : sans ça,
    // toute la phrase finissait jointe en un seul nom introuvable (voir le bug remonté en test).
    private val motsPortionEnTete = setOf(
        "tranche", "tranches", "part", "parts", "verre", "verres", "carré", "carrés",
        "morceau", "morceaux", "rondelle", "rondelles", "tasse", "tasses", "bol", "bols",
        "pot", "pots", "portion", "portions", "salade",
    )

    // Aliments dont la forme courante est le pluriel : le singulier naïf ("pâte", "frite")
    // désigne soit une toute autre chose (la pâte = un aliment différent des pâtes), soit une
    // forme qu'on ne dicte jamais en pratique. On ne les touche pas.
    private val motsPlurielsInvariants = setOf("pâtes", "frites")

    fun analyser(texte: String): List<Pair<String, Int>> {
        // Un "," est toujours collé au mot précédent dans la transcription ("grammes,") : on
        // l'isole en son propre token avant de découper sur les espaces.
        val tokens = texte.lowercase().replace(",", " , ").split(Regex("\\s+")).filter { it.isNotBlank() }

        val segments = mutableListOf(mutableListOf<String>())
        for (mot in tokens) {
            if (mot in motsSeparateurs) {
                segments += mutableListOf<String>()
            } else {
                segments.last() += mot
            }
        }

        return segments.flatMap(::analyserSegment)
    }

    private fun analyserSegment(segment: List<String>): List<Pair<String, Int>> {
        val resultats = mutableListOf<Pair<String, Int>>()
        var motsCourant = mutableListOf<String>()
        var grammagePreciseAuMoinsUneFois = false
        var quantite = 1

        for (mot in segment) {
            val nombre = mot.toIntOrNull()
            if (nombre != null) {
                if (motsCourant.isEmpty() && !grammagePreciseAuMoinsUneFois) {
                    // Nombre en tête, avant tout mot de l'aliment : une quantité ("2 œufs"),
                    // pas un poids (qui, lui, suit toujours le nom : "riz 150 grammes").
                    quantite = nombre
                } else {
                    grammagePreciseAuMoinsUneFois = true
                    val nom = motsCourant.joinToString(" ").trim()
                    if (nom.isNotBlank()) {
                        resultats += resoudreNom(versSingulier(nom), motPortion = null) to nombre
                    }
                    motsCourant = mutableListOf()
                }
            } else if (mot !in motsIgnores) {
                motsCourant += mot
            }
        }

        // Rien après le dernier nombre (cas normal), ou aucun nombre dans tout le segment :
        // dans ce second cas, il n'y a pas de séparateur ("et"/virgule) fiable entre les
        // aliments cités à la suite ("salade de tomates, des olives, des cornichons, un
        // oeuf" est très souvent transcrit SANS virgules par la reconnaissance vocale) —
        // voir resoudreMotsRestants.
        if (!grammagePreciseAuMoinsUneFois && motsCourant.isNotEmpty()) {
            resultats += resoudreMotsRestants(motsCourant, quantite)
        }
        return resultats
    }

    // Sans poids précisé, chaque mot restant est a priori un aliment à part entière (ex.
    // "tomates olives cornichons oeuf" → 4 aliments) : les joindre en un seul nom composé ne
    // correspond à rien dans Ciqual et faisait perdre TOUTE la liste (voir le bug remonté en
    // test réel). Exception : un mot de contenant/portion (motsPortionEnTete, "salade",
    // "bol"...) se combine avec le mot suivant pour former une seule paire, comme avant
    // ("salade tomates" → l'aliment "tomates" avec le grammage d'une salade).
    private fun resoudreMotsRestants(mots: List<String>, quantite: Int): List<Pair<String, Int>> {
        val resultats = mutableListOf<Pair<String, Int>>()
        var index = 0
        while (index < mots.size) {
            val mot = mots[index]
            val motSuivant = mots.getOrNull(index + 1)
            if (mot in motsPortionEnTete && motSuivant != null) {
                val nomNormalise = versSingulier(motSuivant)
                resultats += resoudreNom(nomNormalise, mot) to EstimationGrammage.pour("$mot $nomNormalise") * quantite
                index += 2
            } else {
                val nomNormalise = versSingulier(mot)
                resultats += resoudreNom(nomNormalise, null) to EstimationGrammage.pour(nomNormalise) * quantite
                index += 1
            }
        }
        return resultats
    }

    // Le nom d'un aliment courant (déjà singulier, sans mot de portion) est remplacé par son
    // libellé Ciqual exact quand la table en a un — voir [AlimentsCourants]. Sinon, on renvoie
    // le nom tel quel : TextEntryViewModel retombe alors sur la recherche floue générale.
    private fun resoudreNom(nomBase: String, motPortion: String?): String =
        AlimentsCourants.libellePour(nomBase, motPortion) ?: nomBase

    // La reconnaissance vocale pluralise naturellement un aliment compté ("2 œufs"), mais ni
    // Ciqual ("Oeuf, cru") ni la table de portions (EstimationGrammage) ne connaissent la
    // forme plurielle : on ramène le dernier mot (le nom lui-même) à son singulier probable.
    // Heuristique volontairement simple (pluriel régulier en "s"), pas une vraie analyse
    // grammaticale.
    private fun versSingulier(phrase: String): String {
        val mots = phrase.split(" ")
        val dernier = mots.last()
        if (dernier in motsPlurielsInvariants) return phrase
        val singulier = if (dernier.length > 3 && dernier.endsWith("s") && !dernier.endsWith("ss")) {
            dernier.dropLast(1)
        } else {
            dernier
        }
        return (mots.dropLast(1) + singulier).joinToString(" ")
    }
}
