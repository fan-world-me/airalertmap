package com.airalertmap.data

import android.content.Context
import com.airalertmap.data.model.RegionPolygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads oblast/raion polygon data from bundled assets (no network required).
 * GeoJSON is shipped inside the APK — no download on first launch.
 */
class GeoJsonLoader(private val context: Context) {

    suspend fun loadOblasts(): List<RegionPolygon> = load("oblasts.geojson")

    suspend fun loadRaions(): List<RegionPolygon> = load("raions.geojson")

    private suspend fun load(assetName: String): List<RegionPolygon> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
            parseFeatureCollection(json)
        }.getOrDefault(emptyList())
    }

    private fun parseFeatureCollection(json: String): List<RegionPolygon> {
        val root = JSONObject(json)
        val features = root.optJSONArray("features") ?: return emptyList()
        val result = mutableListOf<RegionPolygon>()

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val props = feature.optJSONObject("properties") ?: JSONObject()
            val key = props.optString("key", "")
            // Oblasts use "region", raions use "rayon"; fall back to "name" / key
            val name = props.optString(
                "region",
                props.optString("rayon", props.optString("name", key))
            )
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

    private fun parsePolygon(coords: JSONArray): List<List<Pair<Double, Double>>> {
        if (coords.length() == 0) return emptyList()
        return listOf(ringToPoints(coords.getJSONArray(0)))
    }

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
        val points = ArrayList<Pair<Double, Double>>(ring.length())
        for (i in 0 until ring.length()) {
            val pt = ring.getJSONArray(i)
            points.add(pt.getDouble(1) to pt.getDouble(0)) // (lat, lon)
        }
        return points
    }
}
