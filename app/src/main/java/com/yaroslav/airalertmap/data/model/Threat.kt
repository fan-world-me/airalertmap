package com.airalertmap.data.model

import com.squareup.moshi.Json

data class ThreatsResponse(
    val serverTime: String = "",
    val threats: List<Threat> = emptyList()
)

data class Threat(
    val id: String = "",
    val type: ThreatType = ThreatType.UNKNOWN,
    val title: String = "Невідома ціль",
    val region: String? = null,
    val district: String? = null,
    val locality: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val heading: Double? = null,
    val confidenceLevel: String? = null,
    val sourceCount: Int? = null,
    val count: Int? = null,
    val updatedAt: String = "",
    val status: ThreatStatus = ThreatStatus.ACTIVE,
    val explanationShort: String? = null,
    val velocity: Velocity? = null,
    val confirmedAt: String? = null,
    val uncertaintyKm: Double? = null,
    val positionQuality: String? = null,
    val advisory: Boolean? = null
)

data class Velocity(
    val bearingDeg: Double? = null,
    val speedKmh: Double? = null
)

enum class ThreatType(val apiValue: String) {
    UAV("uav"),
    FPV("fpv"),
    RECON("recon"),
    MISSILE("missile"),
    BALLISTIC("ballistic"),
    KAB("kab"),
    MIG31K("mig31k"),
    UNKNOWN("unknown");

    companion object {
        fun fromApi(value: String?): ThreatType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class ThreatStatus(val apiValue: String) {
    ACTIVE("active"),
    STALE("stale"),
    RESOLVED("resolved");

    companion object {
        fun fromApi(value: String?): ThreatStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: STALE
    }
}
