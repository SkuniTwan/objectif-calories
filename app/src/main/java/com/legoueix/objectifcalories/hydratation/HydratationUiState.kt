package com.legoueix.objectifcalories.hydratation

import com.legoueix.objectifcalories.hydratation.data.HydratationEntity

data class HydratationUiState(
    val entreesAujourdhui: List<HydratationEntity> = emptyList(),
)

val HydratationUiState.totalClAujourdhui: Int
    get() = entreesAujourdhui.sumOf { it.centilitres }
