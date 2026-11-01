package com.airalertmap.data

import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.DimapTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class AlertsSnapshot(
    val serverTime: String,
    val alertOblasts: List<AlertArea>,
    val alertRaions: List<AlertArea>,
    val targets: List<DimapTarget>
)

class AlertsRepository(
    private val api: DimapApiService = NetworkModule.dimapApi,
    private val pollIntervalMs: Long = 5_000L
) {
    fun observeSnapshot(): Flow<Result<AlertsSnapshot>> = flow {
        while (true) {
            val result = runCatching { api.getPolling() }
            result
                .map { response ->
                    val data = response.data

                    val areas = data.alerts
                        .filter { it.region.isNotBlank() && it.alertType.equals("air", ignoreCase = true) }
                        .map { alert ->
                            AlertArea(
                                key = alert.id.ifBlank { alert.region },
                                name = alert.region,
                                since = alert.startedAt?.toString(),
                                color = alert.color
                            )
                        }

                    AlertsSnapshot(
                        serverTime = data.timestamp,
                        alertOblasts = areas.filterNot { it.name.contains("район", ignoreCase = true) },
                        alertRaions = areas.filter { it.name.contains("район", ignoreCase = true) },
                        targets = data.targets.targets
                    )
                }
                .onSuccess { emit(Result.success(it)) }
                .onFailure { emit(Result.failure(it)) }
            delay(pollIntervalMs)
        }
    }
}
