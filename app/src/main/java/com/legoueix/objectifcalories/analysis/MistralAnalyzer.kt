package com.legoueix.objectifcalories.analysis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Implémentation réelle de [MealAnalyzer]. N'appelle jamais Mistral directement : la
 * photo part vers le relais Cloudflare Worker (voir worker/mistral-relay.js), seul
 * endroit où vit la clé API Mistral — jamais dans l'APK, contrairement à la première
 * version de cette classe (acceptable seulement pour un test privé, voir CLAUDE.md).
 *
 * Le modèle ne renvoie que noms et grammages, jamais de kcal ni de macros : voir
 * [com.legoueix.objectifcalories.correction.MealCaptureViewModel], qui résout chaque
 * nom détecté vers une fiche Ciqual exacte.
 */
class MistralAnalyzer(
    private val workerUrl: String,
    private val appSharedSecret: String,
) : MealAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyze(image: ByteArray): AnalysisResult = withContext(Dispatchers.IO) {
        if (workerUrl.isBlank()) {
            throw IllegalStateException(
                "Aucun relais configuré : renseigne WORKER_URL dans local.properties (voir worker/README.md).",
            )
        }

        val request = Request.Builder()
            .url(workerUrl)
            .header("X-App-Secret", appSharedSecret)
            .post(image.toRequestBody("image/jpeg".toMediaType()))
            .build()

        client.newCall(request).execute().use { reponse ->
            val corps = reponse.body?.string()
            if (!reponse.isSuccessful) {
                throw IOException("Le relais a répondu ${reponse.code} : $corps")
            }
            if (corps.isNullOrBlank()) throw IOException("Réponse du relais vide")
            json.decodeFromString<AnalysisResult>(corps)
        }
    }
}
