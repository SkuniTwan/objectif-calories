package com.legoueix.objectifcalories.poids

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.legoueix.objectifcalories.poids.data.PoidsEntity
import com.legoueix.objectifcalories.poids.data.PoidsRepository
import kotlinx.coroutines.flow.map

/** Dernière pesée connue (la plus récente), ou null si aucune n'a encore été enregistrée. */
@Composable
fun rememberDernierPoidsKg(poidsRepository: PoidsRepository): State<Double?> {
    val flow = remember { poidsRepository.observerHistorique().map { historique -> historique.firstOrNull()?.poidsKg } }
    return flow.collectAsState(initial = null)
}

/** Historique complet des pesées, triées de la plus récente à la plus ancienne. */
@Composable
fun rememberHistoriquePoids(poidsRepository: PoidsRepository): State<List<PoidsEntity>> {
    val flow = remember { poidsRepository.observerHistorique() }
    return flow.collectAsState(initial = emptyList())
}
