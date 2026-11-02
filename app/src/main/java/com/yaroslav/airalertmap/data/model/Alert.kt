package com.airalertmap.data.model

/** A regional alert status provided by DIMAP. */
data class AlertArea(
    val key: String = "",
    val name: String = "Невідома територія",
    val oblast: String = "",
    val since: String? = null,
    val color: String? = null
)

// ── Top-level response ────────────────────────────────────────────────────────

data class DimapPollingResponse(
    val type: String = "",
    val timestamp: String = "",
    val data: DimapPollingData = DimapPollingData()
)

// ── data{} block ──────────────────────────────────────────────────────────────

data class DimapPollingData(
    val targets: DimapTargetsBlock = DimapTargetsBlock(),
    val alerts: List<DimapAlert> = emptyList(),
    val available: List<String> = emptyList(),
    val timestamp: String = ""
)

// ── data.targets{} block ──────────────────────────────────────────────────────

data class DimapTargetsBlock(
    val timestamp: String = "",
    val totalActiveTargets: Int = 0,
    val targets: List<DimapTarget> = emptyList()
)

data class DimapTarget(
    val id: String = "",
    val type: String = "",          // SHAHED | FPV | REACTIVE_SHAHED | …
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val bearing: Int = 0,
    val speed: Int = 0,
    val lastSeen: String = "",
    val history: List<DimapTargetHistory> = emptyList()
)

data class DimapTargetHistory(
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val km: Double = 0.0,
    val time: String = "",
    val at: String = ""
)

// ── data.alerts[] ─────────────────────────────────────────────────────────────

data class DimapAlert(
    val id: String = "",
    val region: String = "",
    val alertType: String = "",
    val color: String = "",
    val startedAt: Long? = null,
    val updatedAt: Long? = null
)
