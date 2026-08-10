package com.airalertmap.data.model

/** A single active official alert area returned by NEPTUN. */
data class AlertArea(
    val key: String = "",
    val name: String = "Невідома територія",
    val oblast: String = "",
    val since: String? = null
)

data class AlertsResponse(
    val raions: List<AlertArea> = emptyList(),
    val oblasts: List<AlertArea> = emptyList()
)
