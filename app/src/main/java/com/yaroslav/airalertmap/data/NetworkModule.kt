package com.airalertmap.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.airalertmap.data.model.ThreatStatus
import com.airalertmap.data.model.ThreatType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private class NeptunJsonAdapters {
    @FromJson
    fun threatTypeFromJson(value: String): ThreatType = ThreatType.fromApi(value)

    @ToJson
    fun threatTypeToJson(value: ThreatType): String = value.apiValue

    @FromJson
    fun threatStatusFromJson(value: String): ThreatStatus = ThreatStatus.fromApi(value)

    @ToJson
    fun threatStatusToJson(value: ThreatStatus): String = value.apiValue
}

object NetworkModule {
    private const val BASE_URL = "https://neptun.in.ua/"

    private val moshi = Moshi.Builder()
        .add(NeptunJsonAdapters())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        // Avoid dumping potentially sensitive live-feed details into Logcat.
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Открытый доступ для GeoJsonLoader — не нужен Moshi, это не JSON:API-схема
    val httpClient: OkHttpClient get() = client

    val neptunApi: NeptunApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NeptunApiService::class.java)
    }
}
