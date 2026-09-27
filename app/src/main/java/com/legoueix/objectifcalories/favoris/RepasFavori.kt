package com.legoueix.objectifcalories.favoris

/**
 * Un repas enregistré comme favori : nom donné par l'utilisateur à la création, photo
 * optionnelle (uniquement si le repas vient du mode Photo/Galerie), et la liste des
 * aliments Ciqual avec leur grammage.
 */
data class RepasFavori(
    val id: String,
    val nom: String,
    val photo: ByteArray?,
    val items: List<AlimentFavori>,
)

/**
 * [codeCiqual] permet de retrouver les valeurs nutritionnelles exactes de l'aliment
 * dans Ciqual au moment où le favori est rappelé — [nom] ne sert qu'à l'affichage.
 */
data class AlimentFavori(
    val nom: String,
    val codeCiqual: String,
    val grammes: Int,
)
