package com.airalertmap.data

import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.Threat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A snapshot can be partial: a temporary failure of /alerts must not hide a
 * successfully received /threats response, and vice versa.
 */
data class AirSnapshot(
    val serverTime: String,
    val threats: List<Threat>,
    val alertOblasts: List<AlertArea>,
    val alertRaions: List<AlertArea>
)

class ThreatsRepository(
    private val api: NeptunApiService = NetworkModule.neptunApi,
    private val pollIntervalMs: Long = 5_000L
) {
    fun observeSnapshot(): Flow<Result<AirSnapshot>> = flow {
        var lastAlertsOblasts = emptyList<AlertArea>()
        var lastAlertsRaions = emptyList<AlertArea>()

        while (true) {
            val threatsResult = runCatching { api.getActiveThreats() }
            val alertsResult = runCatching { api.getAlerts() }

            alertsResult.onSuccess { alerts ->
                lastAlertsOblasts = alerts.oblasts
                lastAlertsRaions = alerts.raions
            }

            threatsResult
                .onSuccess { threats ->
                    emit(
                        Result.success(
                            AirSnapshot(
                                serverTime = threats.serverTime,
                                threats = threats.threats,
                                alertOblasts = lastAlertsOblasts,
                                alertRaions = lastAlertsRaions
                            )
                        )
                    )
                }
                .onFailure { error ->
                    emit(Result.failure(error))
                }

            delay(pollIntervalMs)
        }
    }
}
