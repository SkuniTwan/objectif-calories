package com.legoueix.objectifcalories.analysis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResult(
    val aliments: List<AlimentDetecte>,
    val confiance: Confiance,
)

@Serializable
data class AlimentDetecte(
    val nom: String,
    val grammes: Int,
)

@Serializable
enum class Confiance {
    @SerialName("faible") FAIBLE,
    @SerialName("moyenne") MOYENNE,
    @SerialName("elevee") ELEVEE,
}
