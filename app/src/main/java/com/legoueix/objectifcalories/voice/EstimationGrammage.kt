package com.legoueix.objectifcalories.voice

/**
 * Grammage estimé d'un aliment cité sans poids à la dictée (« un œuf », « une tranche de
 * jambon ») — portions usuelles pour les aliments les plus courants, valeur générique sinon.
 * Purement indicatif : comme le grammage proposé par le modèle photo, il reste ajustable par
 * l'utilisateur sur l'écran de correction.
 *
 * Les clés sont déjà passées par le même filtrage de mots que [DictationParser] (articles et
 * « de »/« d' » retirés) : « tranche de jambon » devient la clé "tranche jambon".
 */
object EstimationGrammage {

    private val portions = mapOf(
        "œuf" to 60,
        "oeuf" to 60,
        "tranche jambon" to 25,
        "tranche pain" to 30,
        "tranche fromage" to 20,
        "banane" to 120,
        "pomme" to 150,
        "orange" to 150,
        "yaourt" to 125,
        "yaourt nature" to 125,
        "part gâteau" to 100,
        "verre lait" to 200,
        "biscuit" to 10,
        "carré chocolat" to 10,
        "pain chocolat" to 70,
        "croissant" to 60,
        "tomate" to 120,
        "avocat" to 150,
    )

    private const val GRAMMES_PAR_DEFAUT = 100

    fun pour(nomAliment: String): Int = portions[nomAliment.lowercase()] ?: GRAMMES_PAR_DEFAUT
}
