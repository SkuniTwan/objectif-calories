package com.legoueix.objectifcalories.textentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legoueix.objectifcalories.ciqual.AlimentEntity
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEBOUNCE_RECHERCHE_MS = 300L

class TextEntryViewModel(
    private val repository: AlimentRepository,
) : ViewModel() {

    private val _lignes = MutableStateFlow(listOf(LigneSaisie()))
    val lignes: StateFlow<List<LigneSaisie>> = _lignes.asStateFlow()

    private val recherchesEnCours = mutableMapOf<String, Job>()

    fun onRechercheChangee(id: String, texte: String) {
        _lignes.update { lignes ->
            lignes.map { ligne ->
                if (ligne.id == id) {
                    ligne.copy(recherche = texte, alimentSelectionne = null, suggestions = emptyList())
                } else {
                    ligne
                }
            }
        }
        recherchesEnCours[id]?.cancel()
        recherchesEnCours[id] = viewModelScope.launch {
            delay(DEBOUNCE_RECHERCHE_MS)
            val resultats = repository.rechercher(texte)
            _lignes.update { lignes ->
                lignes.map { ligne -> if (ligne.id == id) ligne.copy(suggestions = resultats) else ligne }
            }
        }
    }

    fun onAlimentChoisi(id: String, aliment: AlimentEntity) {
        recherchesEnCours[id]?.cancel()
        _lignes.update { lignes ->
            val misesAJour = lignes.map { ligne ->
                if (ligne.id == id) {
                    ligne.copy(recherche = aliment.libelle, alimentSelectionne = aliment, suggestions = emptyList())
                } else {
                    ligne
                }
            }
            // Garantit qu'une ligne vide reste toujours disponible pour la recherche suivante
            // (voir TextEntryScreen, qui l'affiche comme barre de recherche permanente).
            if (misesAJour.none { it.alimentSelectionne == null }) misesAJour + LigneSaisie() else misesAJour
        }
    }

    fun onGrammesChanges(id: String, grammes: Int) {
        _lignes.update { lignes ->
            lignes.map { ligne -> if (ligne.id == id) ligne.copy(grammes = grammes) else ligne }
        }
    }

    fun onAjouterLigne() {
        _lignes.update { it + LigneSaisie() }
    }

    /**
     * Peuple les lignes à partir d'une dictée déjà découpée en paires (nom, grammes) —
     * voir [com.legoueix.objectifcalories.voice.DictationParser]. Une paire qui ne
     * correspond à aucun aliment Ciqual est ignorée plutôt que de créer une ligne vide :
     * le découpage de la dictée n'est pas fiable à 100 % (ex. deux aliments fusionnés
     * en un seul segment faute de nombre entre les deux), une ligne sans correspondance
     * serait de toute façon exclue à la validation, mieux vaut ne pas l'afficher du tout.
     */
    fun definirDepuisDictee(paires: List<Pair<String, Int>>) {
        viewModelScope.launch {
            val lignes = paires.mapNotNull { (nom, grammes) ->
                val suggestions = repository.rechercher(nom)
                val correspondance = suggestions.firstOrNull { it.libelle.equals(nom, ignoreCase = true) }
                    ?: suggestions.firstOrNull()
                correspondance?.let {
                    LigneSaisie(
                        recherche = it.libelle,
                        alimentSelectionne = it,
                        grammes = grammes,
                    )
                }
            }
            // + une ligne vide en fin de liste (voir onAlimentChoisi) : la recherche reste
            // disponible pour ajouter un aliment supplémentaire après la dictée.
            _lignes.value = lignes + LigneSaisie()
        }
    }

    fun onSupprimerLigne(id: String) {
        recherchesEnCours.remove(id)?.cancel()
        _lignes.update { lignes -> lignes.filterNot { it.id == id }.ifEmpty { listOf(LigneSaisie()) } }
    }

    fun reinitialiser() {
        recherchesEnCours.values.forEach { it.cancel() }
        recherchesEnCours.clear()
        _lignes.value = listOf(LigneSaisie())
    }
}
