package com.airalertmap.data

import com.airalertmap.data.model.DimapPollingResponse
import retrofit2.http.GET

interface DimapApiService {
    @GET("api/polling")
    suspend fun getPolling(): DimapPollingResponse
}
