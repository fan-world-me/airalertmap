package com.airalertmap.data

import com.airalertmap.data.model.RegionPolygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Грузит https://neptun.in.ua/oblasts.geojson — границы областей, по которым
 * подсвечиваются полигоны на карте в статусе тревоги. Ключи (`key` в
 * properties) совпадают с AlertArea.key из /api/v1/alerts.
 *
 * GeoJSON — не JSON:API-схема остального проекта (Polygon/MultiPolygon —
 * разной вложенности), поэтому парсим руками через org.json, а не Moshi.
 */
class GeoJsonLoader(
    private val client: OkHttpClient = NetworkModule.httpClient
) {

    suspend fun loadOblasts(): List<RegionPolygon> = loadGeoJson(OBLASTS_URL)

    suspend fun loadRaions(): List<RegionPolygon> = loadGeoJson(RAIONS_URL)

    private suspend fun loadGeoJson(url: String): List<RegionPolygon> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@use emptyList()
                parseFeatureCollection(body)
            }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val OBLASTS_URL = "https://neptun.in.ua/oblasts.geojson"
        const val RAIONS_URL = "https://neptun.in.ua/raions.geojson"
    }

    private fun parseFeatureCollection(json: String): List<RegionPolygon> {
        val root = JSONObject(json)
        val features = root.optJSONArray("features") ?: return emptyList()
        val result = mutableListOf<RegionPolygon>()

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val props = feature.optJSONObject("properties") ?: JSONObject()
            val key = props.optString("key", "")
            val name = props.optString("name", key)
            if (key.isEmpty()) continue

            val geometry = feature.optJSONObject("geometry") ?: continue
            val type = geometry.optString("type")
            val coords = geometry.optJSONArray("coordinates") ?: continue

            val rings: List<List<Pair<Double, Double>>> = when (type) {
                "Polygon" -> parsePolygon(coords)
                "MultiPolygon" -> parseMultiPolygon(coords)
                else -> emptyList()
            }

            if (rings.isNotEmpty()) {
                result.add(RegionPolygon(key = key, name = name, rings = rings))
            }
        }
        return result
    }

    // Polygon: [ [ [lon,lat], ... ] , holes... ] — берём только внешнее кольцо,
    // дырки (эксклавы) для закраски тревоги на мобильной карте не критичны.
    private fun parsePolygon(coords: JSONArray): List<List<Pair<Double, Double>>> {
        if (coords.length() == 0) return emptyList()
        return listOf(ringToPoints(coords.getJSONArray(0)))
    }

    // MultiPolygon: [ Polygon, Polygon, ... ]
    private fun parseMultiPolygon(coords: JSONArray): List<List<Pair<Double, Double>>> {
        val rings = mutableListOf<List<Pair<Double, Double>>>()
        for (i in 0 until coords.length()) {
            val polygon = coords.getJSONArray(i)
            if (polygon.length() == 0) continue
            rings.add(ringToPoints(polygon.getJSONArray(0)))
        }
        return rings
    }

    private fun ringToPoints(ring: JSONArray): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        for (i in 0 until ring.length()) {
            val pt = ring.getJSONArray(i)
            points.add(pt.getDouble(1) to pt.getDouble(0)) // (lat, lon)
        }
        return points
    }
}
