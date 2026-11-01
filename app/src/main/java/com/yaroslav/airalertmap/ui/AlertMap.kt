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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.RegionPolygon
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min

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
    val areaFill: Color,
    val border: Color,
    val raionBorder: Color,
    val oblastAlertFill: Color,
    val raionAlertFill: Color,
    val oblastWarningFill: Color,
    val raionWarningFill: Color,
    val labelColor: Int,
    val cityDot: Int
)

private val DARK_PALETTE = MapPalette(
    background = Color(0xFF10151A),
    areaFill = Color(0xFF1B242A),
    border = Color(0xFF52616A),
    raionBorder = Color(0xFF323E45),
    oblastAlertFill = Color(0xFFBA1A1A),
    raionAlertFill = Color(0xFFEF5350),
    oblastWarningFill = Color(0xFF815000),
    raionWarningFill = Color(0xFFC78300),
    labelColor = 0xFFE1E7EA.toInt(),
    cityDot = 0xFFF3F5F6.toInt()
)

private val LIGHT_PALETTE = MapPalette(
    background = Color(0xFFE8ECEC),
    areaFill = Color(0xFFF1F4F3),
    border = Color(0xFF859394),
    raionBorder = Color(0xFFBCC7C5),
    oblastAlertFill = Color(0xFFBA1A1A),
    raionAlertFill = Color(0xFFE35D56),
    oblastWarningFill = Color(0xFF825500),
    raionWarningFill = Color(0xFFD18C00),
    labelColor = 0xFF253239.toInt(),
    cityDot = 0xFF18252B.toInt()
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

@Composable
fun AlertMap(
    oblastPolygons: List<RegionPolygon> = emptyList(),
    raionPolygons: List<RegionPolygon> = emptyList(),
    alertOblasts: List<AlertArea> = emptyList(),
    alertRaions: List<AlertArea> = emptyList(),
    isDarkMap: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(1f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    val palette = if (isDarkMap) DARK_PALETTE else LIGHT_PALETTE
    val oblastLabelPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = palette.labelColor
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val cityLabelPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = palette.labelColor
            textSize = 32f
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
                .pointerInput(Unit) {
                    detectTapGestures(
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

            for (region in oblastPolygons) {
                val alert = alertForRegion(alertOblasts, region)
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
                    drawPath(
                        path,
                        color = if (alert == null) palette.areaFill else alertFillColor(alert, palette, oblast = true)
                    )
                }
            }

            for (region in raionPolygons) {
                val alert = alertForRegion(alertRaions, region)
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
                    if (alert != null) drawPath(path, color = alertFillColor(alert, palette, oblast = false))
                    drawPath(
                        path,
                        color = palette.raionBorder,
                        style = Stroke(width = if (alert != null) 1.7f else 0.8f)
                    )
                }
            }

            for (region in oblastPolygons) {
                val alert = alertForRegion(alertOblasts, region)
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
                    drawPath(
                        path,
                        color = palette.border,
                        style = Stroke(width = if (alert != null) 2.5f else 1.3f)
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

private fun normalizedRegionName(value: String): String =
    value.lowercase()
        .replace(Regex("(область|області|район|райони|района)$"), "")
        .filter(Char::isLetterOrDigit)

private fun alertForRegion(alerts: List<AlertArea>, region: RegionPolygon): AlertArea? {
    val regionNames = setOf(normalizedRegionName(region.key), normalizedRegionName(region.name))
    return alerts.firstOrNull { alert ->
        normalizedRegionName(alert.key) in regionNames ||
            normalizedRegionName(alert.name) in regionNames
    }
}

private fun alertFillColor(alert: AlertArea?, palette: MapPalette, oblast: Boolean): Color {
    if (alert == null) return palette.areaFill
    val isWarning = alert.color.equals("yellow", ignoreCase = true) ||
        alert.color.equals("warning", ignoreCase = true)
    return when {
        oblast && isWarning -> palette.oblastWarningFill
        isWarning -> palette.raionWarningFill
        oblast -> palette.oblastAlertFill
        else -> palette.raionAlertFill
    }
}
