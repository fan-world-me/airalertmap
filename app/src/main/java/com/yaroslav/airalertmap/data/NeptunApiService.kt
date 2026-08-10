package com.airalertmap.data

import com.airalertmap.data.model.AlertsResponse
import com.airalertmap.data.model.ThreatsResponse
import retrofit2.http.GET

interface NeptunApiService {
    @GET("api/v1/threats")
    suspend fun getActiveThreats(): ThreatsResponse

    @GET("api/v1/alerts")
    suspend fun getAlerts(): AlertsResponse
}
