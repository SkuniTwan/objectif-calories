package com.legoueix.objectifcalories.ciqual

/**
 * Table de synonymes maison pour les aliments les plus courants (voir CLAUDE.md, section
 * « Mapping vers Ciqual ») : la recherche floue générale (LIKE + libellé le plus court, voir
 * [AlimentDao.rechercher]) échoue trop souvent sur des mots usuels dictés à la voix — "riz"
 * matche "Chorizo" (qui contient la sous-chaîne "riz" et a un libellé plus court que toutes
 * les entrées "Riz..."), "pain" matche systématiquement "Pain pita" (le plus court des
 * nombreux libellés "Pain..."). Consultée en priorité par [com.legoueix.objectifcalories.voice.DictationParser]
 * avant de retomber sur la recherche floue si l'aliment n'y figure pas.
 *
 * Les valeurs sont des libellés Ciqual exacts (vérifiés dans ciqual_seed.json) : une
 * correspondance exacte déclenche le chemin rapide dans TextEntryViewModel.definirDepuisDictee
 * plutôt que de dépendre à nouveau de l'heuristique « libellé le plus court ».
 *
 * Choix par défaut quand plusieurs préparations existent : l'état tel qu'on le mangerait dans
 * un repas (riz et pâtes « cuits », pas « crus » — voir CLAUDE.md sur le risque cru/cuit), pas
 * nécessairement le premier résultat alphabétique.
 */
object AlimentsCourants {

    private val correspondances = mapOf(
        "riz" to "Riz blanc, cuit, sans sel ajouté",
        "pâtes" to "Pâtes sèches, standard, cuites, sans sel ajouté",
        "lait" to "Lait entier (aliment moyen)",
        "beurre" to "Beurre à 80% MG minimum, doux",
        "haricots" to "Haricot vert, cuit",
        "haricot" to "Haricot vert, cuit",
        "frites" to "Frites de pommes de terre, surgelées, cuites en friteuse",
        "yaourt" to "Yaourt ou lait fermenté, nature ou aux fruits (aliment moyen)",
        "œuf" to "Oeuf dur",
        "oeuf" to "Oeuf dur",
        "jambon" to "Jambon cuit, choix",
        "banane" to "Banane, chair sans peau, crue",
        "pomme" to "Pomme, chair sans peau, crue (aliment moyen)",
        "orange" to "Orange, chair sans peau, sans pépins, crue",
        "poulet" to "Poulet, viande et peau rôties/cuites au four",
        "fromage" to "Emmental ou emmenthal",
        "tomate" to "Tomate sans précision, crue (aliment moyen)",
        "avocat" to "Avocat, chair sans peau, sans noyau, cru",
        "croissant" to "Croissant, sans précision (aliment moyen)",
        "sucre" to "Sucre blanc",
        "huile" to "Huile d'olive vierge extra",
        "olive" to "Olive (aliment moyen)",
        "cornichon" to "Cornichon, au vinaigre",
        // Sans précision de portion : le pain le plus généralement mangé (baguette).
        "pain" to "Pain blanc (par ex. : baguette, boule…)",
    )

    // Le pain change selon le contenant cité : une tranche évoque du pain de mie, un morceau
    // plutôt de la baguette (repère demandé explicitement, cf. discussion produit).
    private val painSelonPortion = mapOf(
        "tranche" to "Pain de mie blanc, préemballé",
        "tranches" to "Pain de mie blanc, préemballé",
        "morceau" to "Pain blanc (par ex. : baguette, boule…)",
        "morceaux" to "Pain blanc (par ex. : baguette, boule…)",
    )

    /**
     * Libellé Ciqual exact pour [nomAliment] (déjà normalisé par DictationParser : singulier,
     * sans article ni mot de portion), en tenant compte du mot de portion cité le cas échéant.
     * `null` si l'aliment n'est pas dans la table : l'appelant retombe alors sur la recherche
     * floue générale.
     */
    fun libellePour(nomAliment: String, motPortion: String?): String? {
        if (nomAliment == "pain" && motPortion != null) {
            painSelonPortion[motPortion]?.let { return it }
        }
        return correspondances[nomAliment]
    }
}
