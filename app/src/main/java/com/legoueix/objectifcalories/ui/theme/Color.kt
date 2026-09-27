package com.legoueix.objectifcalories.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens repris du design "Bento assiette" (direction 2a), convertis depuis leurs
// valeurs oklch d'origine (voir le handoff design) vers sRGB pour Compose.

// Fond d'écran (crème) — remplace l'ancien fond blanc cassé.
val OffWhite = Color(0xFFFBF7F1)

// Surfaces
val SurfaceAlt = Color(0xFFEFE8DD) // tuile secondaire / fond alterné
val Track = Color(0xFFF1EAE0) // piste de slider, segment inactif

// Encre (texte principal / tuiles sombres de mise en avant)
val Charcoal = Color(0xFF211E1B)
val TextSecondary = Color(0xFF6B655D)
val TextTertiary = Color(0xFF9C9489)
val TextDisabled = Color(0xFFB3AAA0)
val Chevron = Color(0xFFC4BBAF)

// Accent unique (terre cuite) — porte l'action et la progression dans tout le design.
val Terracotta = Color(0xFFCB6440)
val AccentTextOnLight = Color(0xFFB34F2A)
val AccentTextOnApricot = Color(0xFFA24019)

// Bloc Eau — constant dans toute l'app (le bleu veut toujours dire eau).
val SkyBlue = Color(0xFF2A92BB) // "plein" (verres remplis, courbes)
val EauTexte = Color(0xFF4A5A6B)

// Bloc Sport — constant dans toute l'app (l'abricot veut toujours dire sport).
// Le fond abricot pâle des tuiles Sport est obtenu par Terracotta à faible opacité
// (voir les cards des bilans) plutôt que par une couleur dédiée.
val SportTexte = Color(0xFF6B4A3B)

// Terracotta à 16 % opacité, déjà mélangé avec le fond de page (OffWhite) : couleur
// opaque équivalente, à utiliser quand le fond réel n'est pas OffWhite (ex. un cercle
// d'icône sur une card blanche) pour ne pas obtenir un ton différent de celui des
// tuiles Sport, qui elles reposent directement sur le fond de page.
val SportFondPale = Color(0xFFF3DFD5)

// Non redéfini par le nouveau système (icônes Favoris / Calories par sport) :
// conservé tel quel plutôt que rattaché à un token qui n'a pas ce rôle.
val Gold = Color(0xFFCA8A04)

// Macros — jamais réaffectées ailleurs.
val LightOrange = Terracotta // glucides
val LightGreen = Color(0xFF519160) // protéines
val LightYellow = Color(0xFFD8B260) // lipides

// Positif (perte de poids, delta favorable).
val DarkGreen = Color(0xFF21763C)

// Résiduel : total calorique — rattaché à l'accent plutôt qu'à une couleur arbitraire.
val Pink = Terracotta

// Badge de confiance (analyse du repas).
val Amber = Color(0xFF8A5320)

// Neutres restants, sans rôle dans le nouveau système mais toujours référencés :
// rattachés à l'encre pour rester sobres plutôt que de garder une teinte arbitraire.
val Mauve = Charcoal
val Teal = SkyBlue
val SageGreen = Terracotta
val SageGreenLight = SurfaceAlt
val SageGreenMuted = TextSecondary
val Orange = Terracotta

// Erreur / destructif — non redéfini par le design, conservé tel quel.
val Red = Color(0xFFDC2626)
