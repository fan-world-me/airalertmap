package com.airalertmap.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.RegionPolygon
import com.airalertmap.data.model.Threat
import com.airalertmap.data.model.ThreatType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import java.time.Instant

private const val LAT_MIN = 43.9
private const val LAT_MAX = 52.7
private const val LON_MIN = 21.5
private const val LON_MAX = 41.0

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 12f
private const val DOUBLE_TAP_SCALE = 3.2f

private data class CityLabel(val name: String, val lat: Double, val lon: Double)

private val MAJOR_CITIES = listOf(
    CityLabel("Київ", 50.4501, 30.5234),
    CityLabel("Харків", 49.9935, 36.2304),
    CityLabel("Одеса", 46.4825, 30.7233),
    CityLabel("Дніпро", 48.4647, 35.0462),
    CityLabel("Львів", 49.8397, 24.0297),
    CityLabel("Запоріжжя", 47.8388, 35.1396),
    CityLabel("Миколаїв", 46.9750, 31.9946),
    CityLabel("Вінниця", 49.2331, 28.4682),
    CityLabel("Полтава", 49.5883, 34.5514),
    CityLabel("Чернігів", 51.4982, 31.2893),
    CityLabel("Суми", 50.9077, 34.7981),
    CityLabel("Херсон", 46.6354, 32.6169)
)

private data class MapPalette(
    val background: Color,
    val border: Color,
    val raionBorder: Color,
    val oblastAlertFill: Color,
    val raionAlertFill: Color,
    val labelColor: Int,
    val cityDot: Int
)

private val DARK_PALETTE = MapPalette(
    background = Color(0xFF12161D),
    border = Color(0xFF3A4353),
    raionBorder = Color(0xFF232A35),
    oblastAlertFill = Color(0xFFB0392E),
    raionAlertFill = Color(0xFFE0745F),
    labelColor = 0xFFB8C1CC.toInt(),
    cityDot = 0xFFEFEFEF.toInt()
)

private val LIGHT_PALETTE = MapPalette(
    background = Color(0xFFF3F4F6),
    border = Color(0xFFC7CCD4),
    raionBorder = Color(0xFFD6DAE0),
    oblastAlertFill = Color(0xFFE0574A),
    raionAlertFill = Color(0xFFF0958A),
    labelColor = 0xFF5B6470.toInt(),
    cityDot = 0xFF2A2F36.toInt()
)

private data class MapFrame(
    val lonScale: Double,
    val baseScale: Float,
    val baseOffsetX: Float,
    val baseOffsetY: Float
)

private fun buildFrame(w: Float, h: Float): MapFrame {
    val midLatRad = Math.toRadians((LAT_MIN + LAT_MAX) / 2.0)
    val lonScale = cos(midLatRad)
    val contentW = (LON_MAX - LON_MIN) * lonScale
    val contentH = LAT_MAX - LAT_MIN
    val baseScale = min(w / contentW, h / contentH).toFloat()
    val baseOffsetX = (w - contentW.toFloat() * baseScale) / 2f
    val baseOffsetY = (h - contentH.toFloat() * baseScale) / 2f
    return MapFrame(lonScale, baseScale, baseOffsetX, baseOffsetY)
}

private fun project(
    lat: Double,
    lon: Double,
    frame: MapFrame,
    scale: Float,
    offset: Offset
): Offset {
    val px = (lon - LON_MIN) * frame.lonScale * frame.baseScale + frame.baseOffsetX
    val py = (LAT_MAX - lat) * frame.baseScale + frame.baseOffsetY
    return Offset(px.toFloat(), py.toFloat()) * scale + offset
}

private fun colorFor(type: ThreatType): Color = when (type) {
    ThreatType.UAV -> Color(0xFFFFA726)
    ThreatType.FPV -> Color(0xFFFF7043)
    ThreatType.RECON -> Color(0xFFFFD54F)
    ThreatType.MISSILE -> Color(0xFFFF5252)
    ThreatType.BALLISTIC -> Color(0xFFE53935)
    ThreatType.KAB -> Color(0xFFE040FB)
    ThreatType.MIG31K -> Color(0xFF9575CD)
    ThreatType.UNKNOWN -> Color(0xFFB0BEC5)
}

private fun markerBaseRadius(type: ThreatType, selected: Boolean): Float = when (type) {
    ThreatType.UAV -> if (selected) 16f else 13f
    ThreatType.FPV -> if (selected) 13f else 10f
    ThreatType.MISSILE, ThreatType.BALLISTIC, ThreatType.KAB -> if (selected) 14f else 11f
    ThreatType.RECON, ThreatType.MIG31K -> if (selected) 11f else 9f
    ThreatType.UNKNOWN -> if (selected) 9f else 7f
}

private fun markerHitRadius(type: ThreatType, selected: Boolean): Float =
    markerBaseRadius(type, selected) * when (type) {
        ThreatType.RECON, ThreatType.MIG31K, ThreatType.UNKNOWN -> 1.8f
        else -> 2.0f
    }

private fun destination(lat: Double, lon: Double, bearingDeg: Double, distanceKm: Double): Pair<Double, Double> {
    val radiusKm = 6371.0088
    val angular = distanceKm / radiusKm
    val lat1 = Math.toRadians(lat)
    val lon1 = Math.toRadians(lon)
    val bearing = Math.toRadians(bearingDeg)

    val lat2 = asin(sin(lat1) * cos(angular) + cos(lat1) * sin(angular) * cos(bearing))
    val lon2 = lon1 + atan2(
        sin(bearing) * sin(angular) * cos(lat1),
        cos(angular) - sin(lat1) * sin(lat2)
    )
    return Math.toDegrees(lat2) to Math.toDegrees(lon2)
}

private fun predictedLatLon(threat: Threat, nowMs: Long): Pair<Double, Double> {
    val speed = threat.velocity?.speedKmh ?: return threat.lat to threat.lon
    val bearing = threat.velocity.bearingDeg ?: threat.heading ?: return threat.lat to threat.lon
    if (speed <= 0.0) return threat.lat to threat.lon

    val anchor = try {
        Instant.parse(threat.confirmedAt ?: threat.updatedAt).toEpochMilli()
    } catch (_: Exception) {
        nowMs
    }
    val elapsedHours = max(0.0, (nowMs - anchor).toDouble() / 3_600_000.0)
    return destination(threat.lat, threat.lon, bearing, speed * elapsedHours)
}

private fun regionCentroid(rings: List<List<Pair<Double, Double>>>): Pair<Double, Double>? {
    val outer = rings.maxByOrNull { it.size } ?: return null
    if (outer.isEmpty()) return null
    var sumLat = 0.0
    var sumLon = 0.0
    for ((lat, lon) in outer) {
        sumLat += lat
        sumLon += lon
    }
    return (sumLat / outer.size) to (sumLon / outer.size)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawThreatMarker(
    center: Offset,
    type: ThreatType,
    headingDeg: Float,
    advisory: Boolean,
    selected: Boolean
) {
    val color = colorFor(type)
    val alpha = if (advisory) 0.55f else 1f
    val radius = markerBaseRadius(type, selected)

    rotate(degrees = headingDeg, pivot = center) {
        when (type) {
            ThreatType.UAV, ThreatType.FPV -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius * 0.75f, center.y + radius * 0.7f)
                    lineTo(center.x, center.y + radius * 0.3f)
                    lineTo(center.x - radius * 0.75f, center.y + radius * 0.7f)
                    close()
                }
                drawPath(path, color = color.copy(alpha = alpha))
            }
            ThreatType.MISSILE, ThreatType.BALLISTIC, ThreatType.KAB -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius, center.y)
                    lineTo(center.x, center.y + radius)
                    lineTo(center.x - radius, center.y)
                    close()
                }
                drawPath(path, color = color.copy(alpha = alpha))
            }
            ThreatType.RECON, ThreatType.MIG31K -> {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (selected) 3.5f else 2.8f)
                )
            }
            ThreatType.UNKNOWN -> {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = center
                )
            }
        }
    }

    if (type != ThreatType.RECON && type != ThreatType.MIG31K) {
        drawCircle(
            color = Color.White.copy(alpha = 0.35f * alpha),
            radius = 1.5f,
            center = center
        )
    }
}

@Composable
fun ThreatMap(
    threats: List<Threat>,
    oblastPolygons: List<RegionPolygon> = emptyList(),
    raionPolygons: List<RegionPolygon> = emptyList(),
    alertOblasts: List<AlertArea> = emptyList(),
    alertRaions: List<AlertArea> = emptyList(),
    isDarkMap: Boolean = true,
    selectedThreatId: String? = null,
    onThreatClick: (Threat?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(1f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val coroutineScope = rememberCoroutineScope()

    val palette = if (isDarkMap) DARK_PALETTE else LIGHT_PALETTE
    val activeOblastKeys = remember(alertOblasts) { alertOblasts.map { it.key }.toSet() }
    val activeRaionKeys = remember(alertRaions) { alertRaions.map { it.key }.toSet() }

    val oblastLabelPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = palette.labelColor
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            alpha = 150
        }
    }
    val cityLabelPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = palette.labelColor
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    fun clampOffset(scale: Float, offset: Offset, w: Float, h: Float): Offset {
        val maxOffsetX = w * (scale - 1f)
        val maxOffsetY = h * (scale - 1f)
        return Offset(
            x = offset.x.coerceIn(-maxOffsetX, 0f),
            y = offset.y.coerceIn(-maxOffsetY, 0f)
        )
    }

    LaunchedEffect(threats) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(250L)
        }
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val newScale = (scaleAnim.value * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                        val rawOffset = (offsetAnim.value - centroid) * (newScale / scaleAnim.value) + centroid + pan
                        val clamped = clampOffset(newScale, rawOffset, size.width.toFloat(), size.height.toFloat())
                        coroutineScope.launch { scaleAnim.snapTo(newScale) }
                        coroutineScope.launch { offsetAnim.snapTo(clamped) }
                    }
                }
                .pointerInput(threats, selectedThreatId) {
                    detectTapGestures(
                        onTap = { tapPoint ->
                            val frame = buildFrame(size.width.toFloat(), size.height.toFloat())
                            val scale = scaleAnim.value
                            val offset = offsetAnim.value
                            val hit = threats.asReversed().firstOrNull { threat ->
                                val (lat, lon) = predictedLatLon(threat, System.currentTimeMillis())
                                val center = project(lat, lon, frame, scale, offset)
                                val selected = threat.id == selectedThreatId
                                hypot(center.x - tapPoint.x, center.y - tapPoint.y) <= markerHitRadius(threat.type, selected)
                            }
                            onThreatClick(hit)
                        },
                        onDoubleTap = { tapPoint ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val isZoomedIn = scaleAnim.value > MIN_SCALE + 0.1f
                            val targetScale = if (isZoomedIn) MIN_SCALE else DOUBLE_TAP_SCALE
                            val rawOffset = if (isZoomedIn) {
                                Offset.Zero
                            } else {
                                (offsetAnim.value - tapPoint) * (targetScale / scaleAnim.value) + tapPoint
                            }
                            val targetOffset = clampOffset(targetScale, rawOffset, w, h)
                            coroutineScope.launch { scaleAnim.animateTo(targetScale) }
                            coroutineScope.launch { offsetAnim.animateTo(targetOffset) }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val scale = scaleAnim.value
            val offset = offsetAnim.value
            val frame = buildFrame(w, h)

            drawRect(color = palette.background)

            for (region in raionPolygons) {
                val isAlert = region.key in activeRaionKeys
                for (ring in region.rings) {
                    if (ring.size < 3) continue
                    val path = Path().apply {
                        val first = project(ring[0].first, ring[0].second, frame, scale, offset)
                        moveTo(first.x, first.y)
                        for (i in 1 until ring.size) {
                            val p = project(ring[i].first, ring[i].second, frame, scale, offset)
                            lineTo(p.x, p.y)
                        }
                        close()
                    }
                    if (isAlert) drawPath(path, color = palette.raionAlertFill)
                    drawPath(path, color = palette.raionBorder, style = Stroke(width = 0.8f))
                }
            }

            for (region in oblastPolygons) {
                val isAlert = region.key in activeOblastKeys
                for (ring in region.rings) {
                    if (ring.size < 3) continue
                    val path = Path().apply {
                        val first = project(ring[0].first, ring[0].second, frame, scale, offset)
                        moveTo(first.x, first.y)
                        for (i in 1 until ring.size) {
                            val p = project(ring[i].first, ring[i].second, frame, scale, offset)
                            lineTo(p.x, p.y)
                        }
                        close()
                    }
                    if (isAlert) drawPath(path, color = palette.oblastAlertFill)
                    drawPath(
                        path,
                        color = palette.border,
                        style = Stroke(width = if (isAlert) 2.2f else 1.3f)
                    )
                }
            }

            if (scale > 1.6f) {
                for (region in oblastPolygons) {
                    val centroid = regionCentroid(region.rings) ?: continue
                    val p = project(centroid.first, centroid.second, frame, scale, offset)
                    if (p.x in 0f..w && p.y in 0f..h) {
                        drawContext.canvas.nativeCanvas.drawText(region.name, p.x, p.y, oblastLabelPaint)
                    }
                }
            }

            for (city in MAJOR_CITIES) {
                val p = project(city.lat, city.lon, frame, scale, offset)
                if (p.x !in -20f..w + 20f || p.y !in -20f..h + 20f) continue
                drawCircle(color = Color(palette.cityDot), radius = 4f, center = p)
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 4f,
                    center = p,
                    style = Stroke(width = 1.2f)
                )
                drawContext.canvas.nativeCanvas.drawText(city.name, p.x, p.y - 12f, cityLabelPaint)
            }

            for (threat in threats) {
                val (lat, lon) = predictedLatLon(threat, nowMs)
                val center = project(lat, lon, frame, scale, offset)
                val heading = (threat.velocity?.bearingDeg ?: threat.heading ?: 0.0).toFloat()
                val selected = threat.id == selectedThreatId
                drawThreatMarker(center, threat.type, heading, threat.advisory == true, selected)
                if (selected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f),
                        radius = markerBaseRadius(threat.type, true) + 10f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xCC1B222C))
                .clickable {
                    coroutineScope.launch { scaleAnim.animateTo(MIN_SCALE) }
                    coroutineScope.launch { offsetAnim.animateTo(Offset.Zero) }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("↺", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}
