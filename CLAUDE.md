# Journal alimentaire par photo — brief projet

Application Android de suivi calorique. L'utilisateur photographie son assiette,
un modèle multimodal identifie les aliments et estime les grammages, l'app calcule
les apports à partir de la table Ciqual et tient un historique local.

Ce fichier est le point d'entrée du projet : il porte les décisions déjà prises et
les raisons qui les motivent. Les options écartées sont documentées en fin de fichier
pour éviter de les rouvrir sans élément nouveau.

**État : prototype de reconnaissance validé, code applicatif non commencé.**

---

## Le produit en une phrase

Logger un repas en trois secondes à partir d'une photo, sans jugement ni objectif imposé.

La photo est l'argument qui fait installer l'app. Tout le reste — code-barres,
saisie texte, statistiques — est du confort qui vient après. Une décision technique
qui dégrade le parcours photo est une mauvaise décision, même si elle fait économiser
de l'argent.

### Ton produit

Descriptif, pas prescriptif. « Ce mois-ci, 1,5 kg de pâtes » plutôt que
« il vous reste 340 kcal ». Les métriques descriptives retiennent mieux et évitent
de basculer vers le contrôle anxieux, qui est une cause majeure de désinstallation
sur ce segment. Les objectifs caloriques, s'ils arrivent un jour, seront optionnels
et désactivés par défaut.

---

## Décisions techniques

### Modèle de reconnaissance

**Retenu : `mistral-medium-latest`** (Mistral, hébergement UE).
Alternative de secours validée : `gemini-flash-lite-latest`.

Les deux donnent de bons résultats sur des photos réelles. Mistral Medium est retenu
pour l'hébergement européen, qui compte sur des photos de repas nominatives (RGPD,
politique de confidentialité, argument affichable sur le store).

`mistral-small-latest` a été testé avec un prompt durci et reste insuffisant :
il ne décompose pas correctement les plats composés. Ne pas y revenir sans changement
de version du modèle.

Le choix du fournisseur doit rester réversible : voir l'interface `MealAnalyzer` plus bas.

### Coûts observés

Base de calcul : image redimensionnée à 1000 px de côté long, ~1600 tokens en entrée,
~300 en sortie.

| Modèle | Tarif (USD / MTok) | Coût / photo |
|---|---|---|
| mistral-medium-latest | 0,40 in / 2,00 out | ~0,124 ¢ |
| gemini-flash-lite-latest | 0,25 in / 1,50 out | ~0,085 ¢ |

Environ **800 photos pour 1 $**. À 90 photos/utilisateur/mois, 1 000 utilisateurs
actifs coûtent ~111 $/mois sans optimisation, et ~30 $/mois avec la cascade décrite
plus bas.

Ces tarifs bougent : les revérifier sur mistral.ai/pricing avant toute décision
budgétaire.

### Architecture

```
Android (Kotlin)
├── CameraX                capture
├── redimensionnement      1000 px côté long, JPEG q=0.85, AVANT envoi
├── ML Kit                 code-barres + OCR, en local
├── SQLite embarqué        table Ciqual, lecture seule
├── Room                   repas, historique, corrections
└── ─── HTTPS ───► Worker Cloudflare ───► API Mistral
                   (relais mince, protège la clé)
```

**Le backend est volontairement minimal** : il reçoit une image, appelle le modèle,
renvoie le JSON. Pas de base de données côté serveur en V1. Il n'existe que pour
que la clé API ne soit pas dans l'APK, où elle serait extractible par décompilation.

Pas de Couchbase, pas de Postgres, pas de synchronisation. Ça viendra si et seulement
si le multi-appareils devient un besoin réel.

### Redimensionnement côté client

Non négociable, c'est le premier levier de coût. Une photo de smartphone brute en
4000×3000 coûte 3 à 4 fois plus cher que la même en 1000×1000, pour aucun gain de
qualité de reconnaissance sur une assiette.

Formule de coût visuel : `⌈largeur/28⌉ × ⌈hauteur/28⌉` tokens.

### Cascade de résolution

L'utilisateur ne voit qu'un seul bouton appareil photo. Le routage est invisible :

1. **Code-barres détecté dans la photo** → SQLite local (Open Food Facts), gratuit, exact
2. **Étiquette nutritionnelle visible** → OCR ML Kit local, gratuit
3. **Assiette cuisinée** → appel au modèle multimodal
4. **Cache par empreinte d'image** → évite de repayer les photos répétées

Fait tomber le nombre d'appels payants de ~90 à ~25 par utilisateur et par mois.

**À implémenter après le parcours photo nu, pas avant.** Tant que photo → JSON →
écran de correction n'est pas fluide, le reste ne sert à rien.

### Données nutritionnelles

**Ciqual (ANSES)** — fichier XML/Excel à télécharger, pas une API.
3 484 aliments, 74 constituants, valeurs pour 100 g de partie comestible.
Sources : ciqual.anses.fr et recherche.data.gouv.fr (le jeu data.gouv.fr 2020 est
marqué comme remplacé).

Converti en SQLite au moment du build, embarqué dans les assets, interrogé en lecture
seule. Moins d'1 Mo une fois réduit aux champs utiles. Mise à jour à chaque version
de l'app ; Ciqual sort environ une version par an.

**Open Food Facts** pour les produits emballés par code-barres. Licence ODbL :
attribution obligatoire, User-Agent identifiant l'app, et clause *share-alike* à
surveiller si les deux bases sont combinées. Utiliser les exports complets, pas
l'API en direct (15 req/min/IP en lecture).

Pièges au parsing de Ciqual :
- les valeurs sont **cru ou cuit selon la ligne** — « pâtes crues » et « pâtes cuites »
  sont deux entrées très différentes. Le prompt demande au modèle de préciser l'état.
- certaines cellules contiennent `traces`, `-`, ou des valeurs préfixées `<`.
  Le parseur doit les gérer sans planter.

### Le calcul

Le modèle ne fournit **que** le nom de l'aliment et le grammage. Les kcal et macros
viennent de Ciqual, jamais du modèle.

```
kcal_portion = kcal_pour_100g × grammes / 100
```

Raison : les valeurs produites par un LLM sont inventées, donc ni reproductibles
ni auditables. Un utilisateur qui suit son alimentation doit pouvoir savoir d'où
sort le chiffre. Bénéfice secondaire : réponse JSON plus courte, donc moins de
tokens de sortie.

---

## Modèle de données local (Room)

```
Meal      id, timestamp, type, photoUri, provider, model

MealItem  id, mealId, ciqualCode, label, grams,
          kcal, prot, gluc, lip,     ← FIGÉS à l'enregistrement
          gramsSuggested             ← proposition initiale du modèle
```

Trois points structurants :

**Stocker au niveau de l'aliment, pas du repas.** Sinon les statistiques par aliment
(« 1,5 kg de pâtes ce mois-ci ») sont impossibles.

**Figer les valeurs nutritionnelles.** Ne pas les recalculer depuis Ciqual à
l'affichage : une mise à jour de la table modifierait rétroactivement l'historique.
Un journal doit être immuable.

**Conserver `gramsSuggested` à côté de `grams`.** L'écart entre les deux donne le
biais personnel de l'utilisateur. S'il corrige systématiquement les pâtes de 180 à
120 g, appliquer le facteur par défaut la fois suivante. C'est le vrai différenciateur
défendable du produit : au bout de deux semaines l'app est calibrée sur lui, ce qu'aucun
concurrent ne fait. Le modèle, tout le monde peut l'appeler.

**Stocker le `ciqualCode`**, pas seulement le libellé, pour que l'agrégation regroupe
correctement « pâtes », « pâtes cuites » et « spaghettis ».

Récupérer aussi le **groupe alimentaire** Ciqual : il donne gratuitement une deuxième
famille de statistiques (« 40 % de féculents ce mois-ci »), souvent plus actionnable
que le détail par aliment.

Room expose les agrégations en `Flow` pour que les graphiques se rafraîchissent seuls.

---

## Interface fournisseur

Ne jamais appeler l'API d'un fournisseur directement depuis le code métier.

```kotlin
interface MealAnalyzer {
    suspend fun analyze(image: ByteArray): AnalysisResult
}
```

Implémentations : `MistralAnalyzer`, `GeminiAnalyzer`. Le choix se fait par
configuration. Même pattern que le routage multi-moteurs de l'API Search côté pro.

Permet de rebasculer si un fournisseur change ses prix, dégrade sa qualité ou
tombe, et de refaire une comparaison chiffrée sans réécrire l'app.

---

## Le prompt

Version courante, validée sur photos réelles. Le modèle ne renvoie ni kcal ni macros.

```
Tu es un assistant de saisie nutritionnelle. Tu analyses une photo de repas et tu
renvoies la composition estimée.

MÉTHODE
1. Liste chaque aliment que tu vois réellement dans l'assiette.
2. Si tu reconnais un plat cuisiné, ne le renvoie JAMAIS comme un seul item :
   décompose-le en ses ingrédients principaux, avec la part de chacun.
3. Pour chaque ingrédient, estime la masse de la portion VISIBLE en grammes,
   pas la portion standard d'une recette.
4. Compte les matières grasses de cuisson visibles (huile, beurre, crème, fromage
   fondu) comme des items à part entière.
5. Précise l'état de préparation dans le nom quand il change la composition
   (« pâtes cuites », « riz cuit », « poulet rôti »).

EXEMPLE DE DÉCOMPOSITION ATTENDUE
Photo d'un gratin dauphinois. Réponse correcte :
  pomme de terre 220 g · crème fraîche 45 g · lait entier 30 g ·
  emmental râpé 25 g · beurre 8 g
Réponse incorrecte : gratin dauphinois 330 g

REPÈRES DE PORTION
- assiette plate standard : 26 cm de diamètre
- une portion adulte de féculent cuit : 150 à 250 g
- une portion adulte de viande ou poisson : 100 à 180 g
- une cuillère à soupe d'huile : 10 g

CONFIANCE
"elevee" : aliments nets et volumes lisibles.
"moyenne" : un doute sur une portion ou un ingrédient.
"faible" : plat composé difficile à décomposer, éclairage médiocre, aliments masqués.

SORTIE
Réponds UNIQUEMENT avec cet objet JSON. Aucun texte avant ou après, aucune balise
markdown.
{"aliments":[{"nom":"","grammes":0}],"confiance":"faible|moyenne|elevee"}

"nom" est un ingrédient simple en français, au singulier, sans marque.
```

Température 0,2. `response_format: {"type":"json_object"}` côté Mistral,
`responseMimeType: "application/json"` côté Gemini.

**Note :** le prompt du prototype demandait aussi kcal et macros. Cette version les
retire au profit de Ciqual. À revalider sur quelques photos après ce changement.

---

## Mapping vers Ciqual — le morceau qui reste

C'est le sujet non résolu. Le modèle renvoie « emmental râpé », Ciqual contient
« Emmental, râpé » parmi 3 484 libellés très précis (pour « poulet » : *viande et
peau, rôti*, *blanc, cuit*, *cuisse avec peau, rôtie*…).

**Approche retenue pour démarrer :** recherche floue locale via SQLite FTS5, plus une
table de synonymes maison pour les 200 aliments les plus fréquents. Gratuit, hors
ligne, instantané.

**Si insuffisant :** faire proposer 5 candidats par la recherche floue, puis un second
appel au modèle pour trancher. Second appel minuscule en tokens.

Le cas d'erreur le plus coûteux est la confusion cru/cuit — les pâtes crues font
environ trois fois les calories des pâtes cuites. Le prompt demande de préciser
l'état ; le mapping doit en tenir compte explicitement.

---

## Modèle économique

Photo illimitée en gratuit. Ne jamais rationner l'accroche : « 5 photos par jour »
est précisément ce qui fait désinstaller.

Si une monétisation devient nécessaire, deux directions compatibles avec ça :
- **par profondeur** : photo illimitée pour tous, l'abonnement débloque historique
  long, statistiques, export
- **par confort** : modèle rapide en gratuit, modèle plus fin sur les plats complexes
  en payant

Garde-fous à mettre dès le premier jour :
- quota par utilisateur côté backend (ex. 10 photos/jour), protection contre l'abus
- plafond de dépense et alertes dans la console du fournisseur
- cache par empreinte d'image

**BYOK** (clé API de l'utilisateur) : seulement en option dans les réglages avancés,
jamais comme parcours principal — les comptes grand public ne donnent pas de clé API
et il n'existe pas de flux OAuth pour déléguer ces quotas. Si implémenté, stocker la
clé uniquement dans l'Android Keystore et appeler l'API directement depuis le
téléphone, pour n'avoir aucune clé tierce à protéger côté serveur.

---

## Ordre de construction

1. **Parcours photo nu** — capture, redimensionnement, appel, JSON, écran de
   correction des grammages. Rien d'autre. Si ce parcours n'est pas fluide et juste,
   le reste ne compte pas.
2. **Ciqual embarqué + mapping** — remplacer les kcal du modèle par le calcul réel.
3. **Room et historique** — persistance, puis statistiques jour/semaine/mois.
4. **Cascade** — code-barres, OCR, cache.
5. **Apprentissage des corrections** — exploiter `gramsSuggested`.

Deux critères de qualité à surveiller en continu, ce sont eux qui font la rétention :

**Latence.** Un modèle qui répond en 8 secondes tue l'expérience même s'il est plus
juste. Redimensionner côté client, envoyer pendant que l'utilisateur regarde encore
l'écran de capture, afficher un résultat progressif.

**Correction en un geste.** Un slider par aliment, une seconde pour ajuster. La
reconnaissance n'a pas besoin d'être parfaite si la correction est instantanée.

---

## Pistes écartées, avec la raison

| Piste | Pourquoi non |
|---|---|
| **Google Lens** | Pas d'API publique. C'est une app grand public, pas un service développeur. |
| **Cloud Vision / ML Kit pour la reconnaissance** | Renvoie des labels génériques (« food », « dish »). Ni portions, ni décomposition. Reste utile pour code-barres et OCR uniquement. |
| **LogMeal / Passio / Foodvisor** | API dédiées, réelles et fonctionnelles, mais abonnement + verrouillage sur leur base nutritionnelle. À reconsidérer si le mapping Ciqual s'avère trop coûteux à maintenir. Foodvisor est réservé aux clients entreprise sous accord commercial. |
| **mistral-small-latest** | Ne décompose pas les plats composés, même avec un prompt durci et exemples. |
| **pixtral-large-latest** | Même prix que Medium qui donne déjà satisfaction. Aucun besoin non couvert. |
| **Palier gratuit en production (Mistral / Gemini)** | Cadrés comme outils d'évaluation. Chez Gemini, les données du palier gratuit peuvent servir à l'entraînement — rédhibitoire sur des photos de repas nominatives. |
| **Auto-hébergement d'un modèle en poids ouverts** | 100–300 €/mois de GPU. Rentable seulement au-delà de plusieurs dizaines de milliers de photos/mois. |
| **BYOK comme parcours principal** | Voir plus haut : pas de clé sur les comptes grand public, pas d'OAuth, et stocker les clés d'autrui revient à héberger des moyens de paiement. |
| **Couchbase / base serveur en V1** | Ciqual est un fichier figé de moins d'1 Mo, l'historique est personnel. Rien à partager entre utilisateurs. |
| **Laisser le modèle produire les kcal** | Valeurs inventées, non reproductibles, non auditables. |

---

## Outil de comparaison

`banc-essai-assiette.html` — page autonome qui compare les fournisseurs sur la même
photo : latence, tokens réels, coût par appel, et surtout **dérive entre deux appels
identiques** (écart de grammage par aliment, et aliments présents dans un appel mais
pas dans l'autre).

À ressortir pour revalider un changement de modèle ou de prompt. Le critère qui compte
n'est pas « reconnaît-il le poulet » — tous y arrivent — mais la stabilité du JSON et
la reproductibilité des grammages sur la même photo.
