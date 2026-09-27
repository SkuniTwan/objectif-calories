package com.legoueix.objectifcalories.correction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.analysis.Confiance
import com.legoueix.objectifcalories.analysis.ImageResizer
import com.legoueix.objectifcalories.analysis.MealAnalyzer
import com.legoueix.objectifcalories.ciqual.AlimentEntity
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import com.legoueix.objectifcalories.ciqual.versNutrition
import com.legoueix.objectifcalories.historique.AlimentEnregistre
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.openfoodfacts.OpenFoodFactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class MealCaptureViewModel(
    private val analyzer: MealAnalyzer,
    private val alimentRepository: AlimentRepository,
    private val openFoodFactsRepository: OpenFoodFactsRepository,
    private val repasRepository: RepasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(rawPhoto: ByteArray, nomInitial: String = "") {
        _uiState.value = CaptureUiState.Analyzing
        viewModelScope.launch {
            runCatching {
                val resized = ImageResizer.resizeForAnalysis(rawPhoto)
                resized to analyzer.analyze(resized)
            }.onSuccess { (photo, result) ->
                // Chaque nom détecté par le modèle est résolu vers une fiche Ciqual exacte
                // (même logique que la dictée, voir TextEntryViewModel.definirDepuisDictee) :
                // les kcal viennent toujours de Ciqual, jamais du modèle (voir CLAUDE.md).
                // Un aliment sans correspondance est ignoré plutôt que d'inventer ses valeurs.
                val items = result.aliments.mapNotNull { detecte ->
                    val suggestions = alimentRepository.rechercher(detecte.nom)
                    val correspondance = suggestions.firstOrNull { it.libelle.equals(detecte.nom, ignoreCase = true) }
                        ?: suggestions.firstOrNull()
                    correspondance?.let { aliment ->
                        ItemCorrige(
                            nom = aliment.libelle,
                            codeCiqual = aliment.code,
                            grammesSuggeres = detecte.grammes,
                            grammesCorriges = detecte.grammes,
                            nutrition = aliment.versNutrition(),
                        )
                    }
                }
                if (items.isEmpty()) {
                    _uiState.value = CaptureUiState.Error
                } else {
                    _uiState.value = CaptureUiState.Result(
                        photo = photo,
                        items = items,
                        confiance = result.confiance,
                        peutReprendre = true,
                        nomInitial = nomInitial,
                    )
                }
            }.onFailure {
                _uiState.value = CaptureUiState.Error
            }
        }
    }

    fun onGrammesCorriges(nom: String, nouveauxGrammes: Int) {
        _uiState.update { state ->
            if (state !is CaptureUiState.Result) return@update state
            state.copy(
                items = state.items.map { item ->
                    if (item.nom == nom) item.copy(grammesCorriges = nouveauxGrammes) else item
                },
            )
        }
    }

    /**
     * Construit "Votre repas" à partir d'aliments Ciqual déjà résolus — saisie
     * texte/vocale, ou repas favori rappelé (voir [com.legoueix.objectifcalories.ObjectifCaloriesApp],
     * qui résout le code Ciqual de chaque aliment du favori avant d'appeler cette
     * méthode). [photo] n'est renseignée que dans le cas d'un favori qui en a une.
     */
    fun onManualEntryValidated(items: List<Pair<AlimentEntity, Int>>, photo: ByteArray? = null, nom: String = "") {
        _uiState.value = CaptureUiState.Result(
            photo = photo,
            items = items.map { (aliment, grammes) ->
                ItemCorrige(
                    nom = aliment.libelle,
                    codeCiqual = aliment.code,
                    grammesSuggeres = grammes,
                    grammesCorriges = grammes,
                    nutrition = aliment.versNutrition(),
                )
            },
            // Aliments exacts choisis par l'utilisateur (saisie ou favori) : pas de doute du modèle.
            confiance = Confiance.ELEVEE,
            peutReprendre = false,
            nomInitial = nom,
        )
    }

    fun onCodeBarresDetecte(codeBarres: String) {
        _uiState.value = CaptureUiState.Analyzing
        viewModelScope.launch {
            val produit = openFoodFactsRepository.rechercherParCodeBarres(codeBarres)
            if (produit != null) {
                _uiState.value = CaptureUiState.Result(
                    photo = null,
                    items = listOf(
                        ItemCorrige(
                            nom = produit.nom,
                            grammesSuggeres = 100,
                            grammesCorriges = 100,
                            nutrition = produit.nutrition,
                        ),
                    ),
                    // Produit identifié par son code-barres exact : pas de doute du modèle.
                    confiance = Confiance.ELEVEE,
                    // Scan en direct depuis la caméra : "Reprendre" garde son sens ici.
                    peutReprendre = true,
                )
            } else {
                _uiState.value = CaptureUiState.Error
            }
        }
    }

    fun onRetakePhoto() {
        _uiState.value = CaptureUiState.Idle
    }

    fun onValidate(nom: String, dateHeure: Long) {
        val state = _uiState.value
        if (state is CaptureUiState.Result) {
            val nomFinal = nom.ifBlank { nomParDefaut(dateHeure) }
            val items = state.items.map {
                AlimentEnregistre(
                    nom = it.nom,
                    codeCiqual = it.codeCiqual,
                    grammesSuggeres = it.grammesSuggeres,
                    grammes = it.grammesCorriges,
                    nutrition = it.nutrition,
                )
            }
            viewModelScope.launch {
                repasRepository.enregistrer(nomFinal, dateHeure, state.photo, items)
            }
        }
        _uiState.value = CaptureUiState.Idle
    }

    // Nom utilisé quand l'utilisateur laisse le champ vide, pour que l'historique reste
    // lisible sans obliger personne à nommer son repas (voir CLAUDE.md : le parcours photo
    // doit rester sans friction).
    private fun nomParDefaut(dateHeure: Long): String {
        val heure = Instant.ofEpochMilli(dateHeure).atZone(ZoneId.systemDefault()).hour
        return when (heure) {
            in 5..10 -> "Petit-déjeuner"
            in 11..14 -> "Déjeuner"
            in 15..18 -> "Goûter"
            in 19..22 -> "Dîner"
            else -> "Repas"
        }
    }
}
