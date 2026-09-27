# Relais Mistral — déploiement

Remplace l'appel direct téléphone → Mistral (clé API embarquée dans l'APK, acceptable
seulement pour un test privé) par téléphone → Worker → Mistral. La clé Mistral ne
quitte plus jamais ce Worker.

## Prérequis

- Un compte Cloudflare (gratuit).
- Node.js installé, puis : `npm install -g wrangler`
- Ta clé API Mistral (celle déjà dans `local.properties` du projet Android).

## Déploiement

Depuis ce dossier (`worker/`) :

```bash
wrangler login
wrangler deploy
```

Puis configure les deux secrets (jamais dans `wrangler.toml`, jamais commités) :

```bash
wrangler secret put MISTRAL_API_KEY
# colle ta clé Mistral quand demandé

wrangler secret put APP_SHARED_SECRET
# colle la valeur ci-dessous quand demandé
```

Secret partagé app↔Worker généré pour ce projet (à coller tel quel) :
`86f52f78df28c948a1b87c46cce3576e70f26d2b246fc413`

`wrangler deploy` affiche l'URL du Worker (`https://objectif-calories-relay.<ton-compte>.workers.dev`).
Colle-la dans `local.properties` à la racine du projet Android :

```
WORKER_URL=https://objectif-calories-relay.<ton-compte>.workers.dev
APP_SHARED_SECRET=86f52f78df28c948a1b87c46cce3576e70f26d2b246fc413
```

## Vérifier que ça marche

```bash
curl -i -X POST "$WORKER_URL" \
  -H "Content-Type: image/jpeg" \
  -H "X-App-Secret: 86f52f78df28c948a1b87c46cce3576e70f26d2b246fc413" \
  --data-binary @une_photo_de_repas.jpg
```

Doit répondre `200` avec `{"aliments":[...],"confiance":"..."}`. Une erreur `401`
signifie que `APP_SHARED_SECRET` ne correspond pas ; `502` que Mistral a refusé la
requête (vérifie `MISTRAL_API_KEY`).

## Garde-fous pas encore en place

Ce relais protège la clé API, mais pas encore contre l'abus en volume (voir
CLAUDE.md, section « Modèle économique ») :

- **Plafond de dépense** — à configurer dans la console Mistral (alertes + plafond
  mensuel), indépendamment du code.
- **Quota par utilisateur/IP** (ex. 10 photos/jour) — demanderait une couche de
  comptage (Cloudflare KV ou D1), pas encore implémentée ici. À ajouter avant une
  diffusion au-delà d'un cercle restreint de testeurs.
