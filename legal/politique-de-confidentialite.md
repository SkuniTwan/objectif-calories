# Politique de confidentialité — Objectif Calories

*Dernière mise à jour : 27 septembre 2026*

> Source de travail — la version publiée (celle à donner à Google Play) est
> [docs/index.html](../docs/index.html), servie par GitHub Pages. Garder les deux
> synchronisées si l'une change.

Objectif Calories est une application de suivi alimentaire. Cette page explique
quelles données l'application traite, pourquoi, et comment elles sont protégées.

## Éditeur

Antoine Legoueix
Contact : twan.applis@gmail.com

## Aucun compte, aucune publicité, aucun traceur publicitaire

Objectif Calories ne demande pas de créer de compte, n'affiche aucune publicité et
n'intègre aucun outil de mesure d'audience ou de traçage publicitaire tiers.

## Données traitées

### Stockées uniquement sur ton téléphone

L'historique des repas, les pesées, l'hydratation et les activités sportives que tu
enregistres sont stockés **localement sur ton appareil** (base de données SQLite
intégrée à l'application). Rien de tout cela n'est envoyé vers un serveur d'Objectif
Calories — il n'en existe pas. Supprimer une entrée dans l'app, ou désinstaller
l'app, efface ces données.

Si la sauvegarde automatique Android est activée sur ton téléphone, ces données
peuvent être incluses dans ta sauvegarde Google personnelle — au même titre que les
autres applications, ce mécanisme est géré par Android, pas par Objectif Calories.

### Photo d'un repas

Quand tu photographies une assiette pour la faire reconnaître automatiquement,
l'image est redimensionnée sur ton téléphone puis envoyée à un service de reconnaissance
d'aliments basé sur l'intelligence artificielle (Mistral AI, hébergé dans l'Union
européenne) via un relais technique que nous exploitons. Ce relais ne conserve pas
les photos : il les transmet et renvoie la liste des aliments reconnus.

La photo n'est **pas** utilisée pour entraîner un modèle, et n'est associée à
aucune identité — l'app n'a pas de compte utilisateur.

### Dictée vocale

Si tu utilises la saisie vocale, l'enregistrement est traité par le service de
reconnaissance vocale intégré à ton téléphone (fourni par le système Android /
Google), selon les conditions de confidentialité de ton appareil — Objectif Calories
ne reçoit que le texte résultant, jamais l'enregistrement audio lui-même.

### Scan de code-barres

Quand tu scannes le code-barres d'un produit emballé, il est envoyé à la base
publique **Open Food Facts** (open​food​facts.org) pour retrouver le produit
correspondant. Open Food Facts est un projet à but non lucratif et associatif ;
consulte sa propre politique de confidentialité sur son site pour le détail de son
traitement.

### Permissions demandées

- **Appareil photo** : pour photographier un repas.
- **Micro** : pour la dictée vocale (uniquement pendant que tu dictes).
- **Photos/Galerie** : pour importer une photo existante plutôt que d'en prendre une
  nouvelle.
- **Internet** : pour les deux appels décrits ci-dessus (reconnaissance photo, scan
  code-barres).

Aucune de ces permissions n'est utilisée en arrière-plan : chacune n'est sollicitée
qu'au moment où tu utilises la fonctionnalité correspondante.

## Tes droits

Comme l'essentiel de tes données reste sur ton téléphone, tu en gardes le contrôle
direct : tu peux les consulter et les supprimer dans l'application (chaque écran
d'historique propose une suppression), ou tout effacer d'un coup en désinstallant
l'app.

Pour toute question sur cette politique, écris à twan.applis@gmail.com.

## Modifications

Cette politique peut être mise à jour si l'application évolue (nouvelle
fonctionnalité impliquant un nouveau traitement de données, par exemple). La date en
haut de page indique la dernière mise à jour.
