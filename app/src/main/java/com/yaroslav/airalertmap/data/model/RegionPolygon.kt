package com.airalertmap.data.model

/**
 * Один регион (область) как набор колец в lat/lon, как они лежат в
 * oblasts.geojson (см. neptun.in.ua/developers). `key` совпадает с
 * AlertArea.key из /api/v1/alerts — по нему матчится подсветка тревоги.
 */
data class RegionPolygon(
    val key: String,
    val name: String,
    val rings: List<List<Pair<Double, Double>>> // каждое кольцо: список (lat, lon)
)
