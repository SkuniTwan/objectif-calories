/**
 * Relais Cloudflare Worker entre l'app Android et l'API Mistral (voir CLAUDE.md,
 * section « Architecture »). Reçoit une photo, appelle le modèle, renvoie le JSON
 * attendu par l'app — { aliments: [{nom, grammes}], confiance }. C'est le SEUL endroit
 * où la clé API Mistral existe : elle ne quitte jamais ce Worker, contrairement à
 * l'ancienne version qui l'embarquait dans l'APK (extractible par décompilation).
 *
 * Le prompt vit ici plutôt que dans l'app : le faire évoluer ne demande plus de
 * publier une nouvelle version sur le Play Store.
 *
 * Déploiement : voir worker/README.md.
 */

const MISTRAL_URL = "https://api.mistral.ai/v1/chat/completions";
const MODEL = "mistral-medium-latest";

// Limite de taille du corps de requête : l'app redimensionne déjà côté client à
// 1000px/JPEG q=0.85 (quelques centaines de Ko), donc une image plus grosse que ça
// signale un client qui contourne l'app plutôt qu'un usage normal — on refuse plutôt
// que de payer l'appel modèle pour rien.
const TAILLE_MAX_OCTETS = 4 * 1024 * 1024;

const PROMPT = `Tu es un assistant de saisie nutritionnelle. Tu analyses une photo de repas et tu
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

"nom" est un ingrédient simple en français, au singulier, sans marque.`;

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return jsonError("Méthode non supportée", 405);
    }

    // Frein basique contre l'abus par un client qui contournerait l'app (voir
    // CLAUDE.md « garde-fous ») : un secret partagé, embarqué dans l'app comme le
    // reste de son code — extractible par un attaquant motivé, mais élimine le
    // scraping/abus opportuniste de l'URL du Worker trouvée par hasard.
    // Garde-fou complémentaire à ajouter avant une diffusion large : quota par
    // utilisateur (Cloudflare KV) et plafond de dépense côté console Mistral,
    // voir CLAUDE.md « Modèle économique ».
    const secretRecu = request.headers.get("X-App-Secret");
    if (!env.APP_SHARED_SECRET || secretRecu !== env.APP_SHARED_SECRET) {
      return jsonError("Non autorisé", 401);
    }

    const contentType = request.headers.get("Content-Type") || "";
    if (!contentType.startsWith("image/")) {
      return jsonError("Content-Type attendu : image/jpeg", 400);
    }

    const image = await request.arrayBuffer();
    if (image.byteLength === 0) {
      return jsonError("Image vide", 400);
    }
    if (image.byteLength > TAILLE_MAX_OCTETS) {
      return jsonError("Image trop volumineuse", 413);
    }

    const base64 = arrayBufferVersBase64(image);
    const mimeType = contentType.split(";")[0].trim();

    const reponseMistral = await fetch(MISTRAL_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${env.MISTRAL_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: MODEL,
        temperature: 0.2,
        response_format: { type: "json_object" },
        messages: [
          {
            role: "user",
            content: [
              { type: "text", text: PROMPT },
              { type: "image_url", image_url: `data:${mimeType};base64,${base64}` },
            ],
          },
        ],
      }),
    });

    if (!reponseMistral.ok) {
      const corps = await reponseMistral.text();
      console.error("Erreur Mistral", reponseMistral.status, corps);
      return jsonError("Le modèle n'a pas pu analyser cette photo", 502);
    }

    const donnees = await reponseMistral.json();
    const contenu = donnees?.choices?.[0]?.message?.content;
    if (!contenu) {
      return jsonError("Réponse du modèle vide", 502);
    }

    // Le contenu est déjà le JSON {aliments, confiance} attendu par l'app (voir
    // response_format ci-dessus) : on le relaie tel quel, pas de reformattage.
    return new Response(contenu, {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  },
};

function jsonError(message, status) {
  return new Response(JSON.stringify({ erreur: message }), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function arrayBufferVersBase64(buffer) {
  let binaire = "";
  const octets = new Uint8Array(buffer);
  const tailleBloc = 0x8000;
  for (let i = 0; i < octets.length; i += tailleBloc) {
    binaire += String.fromCharCode.apply(null, octets.subarray(i, i + tailleBloc));
  }
  return btoa(binaire);
}
