package com.airalertmap.data.model

/**
 * Один регион (область) как набор колец в lat/lon, как они лежат в
 * oblasts.geojson (см. neptun.in.ua/developers). Статусы тревог связываются
 * с полигонами по нормализованному названию территории.
 */
data class RegionPolygon(
    val key: String,
    val name: String,
    val rings: List<List<Pair<Double, Double>>> // каждое кольцо: список (lat, lon)
)
