package com.airalertmap.data.model

/** A regional alert status provided by DIMAP. */
data class AlertArea(
    val key: String = "",
    val name: String = "Невідома територія",
    val oblast: String = "",
    val since: String? = null,
    val color: String? = null
)

data class DimapPollingResponse(
    val timestamp: String = "",
    val alerts: List<DimapAlert> = emptyList()
)

data class DimapAlert(
    val id: String = "",
    val region: String = "",
    val alertType: String = "",
    val color: String = "",
    val startedAt: Long? = null,
    val updatedAt: Long? = null
)
