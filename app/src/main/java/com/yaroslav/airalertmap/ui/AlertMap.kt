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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.DimapTarget
import com.airalertmap.data.model.RegionPolygon
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min

// ── Map bounds ────────────────────────────────────────────────────────────────
private const val LAT_MIN = 43.9
private const val LAT_MAX = 52.7
private const val LON_MIN = 21.5
private const val LON_MAX = 41.0

private const val MIN_SCALE       = 1f
private const val MAX_SCALE       = 14f
private const val DOUBLE_TAP_SCALE = 3.5f

// ── City labels ───────────────────────────────────────────────────────────────
private data class CityLabel(val name: String, val lat: Double, val lon: Double)

private val MAJOR_CITIES = listOf(
    CityLabel("Київ",       50.4501, 30.5234),
    CityLabel("Харків",     49.9935, 36.2304),
    CityLabel("Одеса",      46.4825, 30.7233),
    CityLabel("Дніпро",     48.4647, 35.0462),
    CityLabel("Львів",      49.8397, 24.0297),
    CityLabel("Запоріжжя",  47.8388, 35.1396),
    CityLabel("Миколаїв",   46.9750, 31.9946),
    CityLabel("Вінниця",    49.2331, 28.4682),
    CityLabel("Полтава",    49.5883, 34.5514),
    CityLabel("Чернігів",   51.4982, 31.2893),
    CityLabel("Суми",       50.9077, 34.7981),
    CityLabel("Херсон",     46.6354, 32.6169),
    CityLabel("Житомир",    50.2547, 28.6587),
    CityLabel("Черкаси",    49.4445, 32.0598),
    CityLabel("Рівне",      50.6197, 26.2516),
    CityLabel("Ужгород",    48.6208, 22.2879),
    CityLabel("Луцьк",      50.7472, 25.3254),
    CityLabel("Тернопіль",  49.5535, 25.5948),
    CityLabel("Хмельницький",49.4220,26.9970),
    CityLabel("Кропивницький",48.5132,32.2597)
)

// ── Map palette ───────────────────────────────────────────────────────────────
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
    background        = Color(0xFF0E1318),
    areaFill          = Color(0xFF1E2B35),
    border            = Color(0xFF4A6272),
    raionBorder       = Color(0xFF2C3E4A),
    oblastAlertFill   = Color(0xFF7A1A1A),
    raionAlertFill    = Color(0xFFAA2828),
    oblastWarningFill = Color(0xFF6B4B00),
    raionWarningFill  = Color(0xFF9A6E00),
    labelColor        = 0xFFCDD8DE.toInt(),
    cityDot           = 0xFFEEF4F7.toInt()
)

private val LIGHT_PALETTE = MapPalette(
    background        = Color(0xFFCDD8DE),
    areaFill          = Color(0xFFDDE8EC),
    border            = Color(0xFF7A9BAA),
    raionBorder       = Color(0xFFADC5CD),
    oblastAlertFill   = Color(0xFFBA1A1A),
    raionAlertFill    = Color(0xFFD94040),
    oblastWarningFill = Color(0xFF825500),
    raionWarningFill  = Color(0xFFBF7C00),
    labelColor        = 0xFF1A2A33.toInt(),
    cityDot           = 0xFF0D1F28.toInt()
)

// ── Projection ────────────────────────────────────────────────────────────────
private data class MapFrame(
    val lonScale: Double,
    val baseScale: Float,
    val baseOffsetX: Float,
    val baseOffsetY: Float
)

private fun buildFrame(w: Float, h: Float): MapFrame {
    val midLatRad = Math.toRadians((LAT_MIN + LAT_MAX) / 2.0)
    val lonScale  = cos(midLatRad)
    val contentW  = (LON_MAX - LON_MIN) * lonScale
    val contentH  = LAT_MAX - LAT_MIN
    val baseScale = min(w / contentW, h / contentH).toFloat()
    val baseOffX  = (w - contentW.toFloat() * baseScale) / 2f
    val baseOffY  = (h - contentH.toFloat() * baseScale) / 2f
    return MapFrame(lonScale, baseScale, baseOffX, baseOffY)
}

private fun project(lat: Double, lon: Double, frame: MapFrame, scale: Float, offset: Offset): Offset {
    val px = (lon - LON_MIN) * frame.lonScale * frame.baseScale + frame.baseOffsetX
    val py = (LAT_MAX - lat) * frame.baseScale + frame.baseOffsetY
    return Offset(px.toFloat(), py.toFloat()) * scale + offset
}

// ── Alert lookup ──────────────────────────────────────────────────────────────
private val SUFFIX_RE = Regex(
    "(\\s*(область|областi|район|райони|района|районі|міський|місто|м\\.))\\s*$",
    RegexOption.IGNORE_CASE
)

private fun normName(v: String): String =
    v.trim().lowercase().replace(SUFFIX_RE, "").trim().filter { it.isLetterOrDigit() }

private fun buildAlertMap(alerts: List<AlertArea>): Map<String, AlertArea> {
    val m = HashMap<String, AlertArea>(alerts.size * 2)
    for (a in alerts) { m[normName(a.key)] = a; m[normName(a.name)] = a }
    return m
}

private fun alertLookup(map: Map<String, AlertArea>, region: RegionPolygon): AlertArea? =
    map[normName(region.key)] ?: map[normName(region.name)]

private fun regionCentroid(rings: List<List<Pair<Double, Double>>>): Pair<Double, Double>? {
    val outer = rings.maxByOrNull { it.size } ?: return null
    if (outer.isEmpty()) return null
    var sLat = 0.0; var sLon = 0.0
    for ((lat, lon) in outer) { sLat += lat; sLon += lon }
    return (sLat / outer.size) to (sLon / outer.size)
}

private fun alertFillColor(alert: AlertArea?, palette: MapPalette, oblast: Boolean): Color {
    if (alert == null) return palette.areaFill
    val warn = alert.color.equals("yellow", ignoreCase = true) ||
               alert.color.equals("warning", ignoreCase = true)
    return when {
        oblast && warn -> palette.oblastWarningFill
        warn           -> palette.raionWarningFill
        oblast         -> palette.oblastAlertFill
        else           -> palette.raionAlertFill
    }
}

// ── Target drawing — aircraft/shahed silhouettes ──────────────────────────────

/**
 * Draws a clean aircraft silhouette: a dart/arrowhead shape pointing "up" (bearing 0).
 * Rotated to actual bearing. Size ~52×52px logical units.
 */
private fun DrawScope.drawAircraftSilhouette(center: Offset, bearing: Int, color: Color, scale: Float) {
    val s = scale.coerceIn(0.6f, 2.5f)
    rotate(degrees = bearing.toFloat(), pivot = center) {
        // Main fuselage dart
        val body = Path().apply {
            moveTo(center.x,            center.y - 26f * s)  // nose tip
            lineTo(center.x - 14f * s, center.y + 10f * s)  // left wing root
            lineTo(center.x -  5f * s, center.y +  6f * s)  // fuselage notch L
            lineTo(center.x -  5f * s, center.y + 26f * s)  // tail L
            lineTo(center.x +  5f * s, center.y + 26f * s)  // tail R
            lineTo(center.x +  5f * s, center.y +  6f * s)  // fuselage notch R
            lineTo(center.x + 14f * s, center.y + 10f * s)  // right wing root
            close()
        }
        drawPath(body, color = color)

        // Wing sweep lines for detail
        drawPath(body, color = Color.White.copy(alpha = 0.25f), style = Stroke(width = 1.2f * s))
    }
}

/**
 * Shahed: a rounder delta-wing shape with no cockpit (drone silhouette).
 */
private fun DrawScope.drawShahedSilhouette(center: Offset, bearing: Int, color: Color, scale: Float) {
    val s = scale.coerceIn(0.6f, 2.5f)
    rotate(degrees = bearing.toFloat(), pivot = center) {
        val body = Path().apply {
            moveTo(center.x,            center.y - 22f * s)  // nose
            lineTo(center.x - 22f * s, center.y + 16f * s)  // left wing tip
            lineTo(center.x - 10f * s, center.y + 10f * s)  // left inner
            lineTo(center.x -  4f * s, center.y + 22f * s)  // tail L
            lineTo(center.x +  4f * s, center.y + 22f * s)  // tail R
            lineTo(center.x + 10f * s, center.y + 10f * s)  // right inner
            lineTo(center.x + 22f * s, center.y + 16f * s)  // right wing tip
            close()
        }
        drawPath(body, color = color)
        drawPath(body, color = Color.White.copy(alpha = 0.2f), style = Stroke(width = 1.2f * s))
    }
}

/**
 * FPV/rocket: small arrowhead.
 */
private fun DrawScope.drawFpvSilhouette(center: Offset, bearing: Int, color: Color, scale: Float) {
    val s = scale.coerceIn(0.6f, 2.5f)
    rotate(degrees = bearing.toFloat(), pivot = center) {
        val body = Path().apply {
            moveTo(center.x,            center.y - 20f * s)  // tip
            lineTo(center.x - 10f * s, center.y + 14f * s)  // left
            lineTo(center.x,            center.y +  8f * s)  // notch
            lineTo(center.x + 10f * s, center.y + 14f * s)  // right
            close()
        }
        drawPath(body, color = color)
    }
}

private fun targetSilhouetteColor(type: String, isDark: Boolean): Color {
    // In dark mode: white silhouettes like ukrainealarm; in light: dark grey
    return if (isDark) Color.White else Color(0xFF1A2A33)
}

// ── Pre-built polygon paths ───────────────────────────────────────────────────
private data class RegionPath(val region: RegionPolygon, val paths: List<Path>)

private fun buildPaths(
    polygons: List<RegionPolygon>,
    frame: MapFrame,
    scale: Float,
    offset: Offset
): List<RegionPath> = polygons.map { region ->
    RegionPath(region, region.rings.mapNotNull { ring ->
        if (ring.size < 3) null
        else Path().apply {
            val f = project(ring[0].first, ring[0].second, frame, scale, offset)
            moveTo(f.x, f.y)
            for (i in 1 until ring.size) {
                val p = project(ring[i].first, ring[i].second, frame, scale, offset)
                lineTo(p.x, p.y)
            }
            close()
        }
    })
}

// ── Main composable ───────────────────────────────────────────────────────────
@Composable
fun AlertMap(
    oblastPolygons: List<RegionPolygon> = emptyList(),
    raionPolygons:  List<RegionPolygon> = emptyList(),
    alertOblasts:   List<AlertArea>     = emptyList(),
    alertRaions:    List<AlertArea>     = emptyList(),
    targets:        List<DimapTarget>   = emptyList(),
    isDarkMap:      Boolean             = true,
    modifier:       Modifier            = Modifier
) {
    val scaleAnim  = remember { Animatable(1f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope      = rememberCoroutineScope()
    val palette    = if (isDarkMap) DARK_PALETTE else LIGHT_PALETTE

    val cityPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color       = palette.labelColor
            textSize    = 32f
            textAlign   = android.graphics.Paint.Align.LEFT
            typeface    = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(3f, 1f, 1f,
                if (isDarkMap) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    val oblastPaint = remember(isDarkMap) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color       = palette.labelColor
            textSize    = 26f
            textAlign   = android.graphics.Paint.Align.CENTER
            typeface    = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(4f, 1f, 1f,
                if (isDarkMap) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }

    fun clampOffset(s: Float, off: Offset, w: Float, h: Float) = Offset(
        off.x.coerceIn(-(w * (s - 1f)), 0f),
        off.y.coerceIn(-(h * (s - 1f)), 0f)
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val ns = (scaleAnim.value * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                        val raw = (offsetAnim.value - centroid) * (ns / scaleAnim.value) + centroid + pan
                        scope.launch { scaleAnim.snapTo(ns) }
                        scope.launch { offsetAnim.snapTo(clampOffset(ns, raw, size.width.toFloat(), size.height.toFloat())) }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tap ->
                        val w = size.width.toFloat(); val h = size.height.toFloat()
                        val zIn = scaleAnim.value > MIN_SCALE + 0.1f
                        val ts = if (zIn) MIN_SCALE else DOUBLE_TAP_SCALE
                        val raw = if (zIn) Offset.Zero
                                  else (offsetAnim.value - tap) * (ts / scaleAnim.value) + tap
                        scope.launch { scaleAnim.animateTo(ts) }
                        scope.launch { offsetAnim.animateTo(clampOffset(ts, raw, w, h)) }
                    })
                }
        ) {
            val w = size.width; val h = size.height
            val sc = scaleAnim.value; val off = offsetAnim.value
            val frame = buildFrame(w, h)

            val oblastAlertMap = buildAlertMap(alertOblasts)
            val raionAlertMap  = buildAlertMap(alertRaions)
            val oblastPaths    = buildPaths(oblastPolygons, frame, sc, off)
            val raionPaths     = buildPaths(raionPolygons,  frame, sc, off)

            // Background
            drawRect(color = palette.background)

            // 1. Oblast fills
            for (rp in oblastPaths) {
                val alert = alertLookup(oblastAlertMap, rp.region)
                val fill  = alertFillColor(alert, palette, oblast = true)
                for (p in rp.paths) drawPath(p, color = fill)
            }

            // 2. Raion fills + thin borders
            for (rp in raionPaths) {
                val alert = alertLookup(raionAlertMap, rp.region)
                for (p in rp.paths) {
                    if (alert != null) drawPath(p, color = alertFillColor(alert, palette, false))
                    drawPath(p, color = palette.raionBorder, style = Stroke(width = if (alert != null) 1.8f else 0.7f))
                }
            }

            // 3. Oblast borders
            for (rp in oblastPaths) {
                val alert = alertLookup(oblastAlertMap, rp.region)
                for (p in rp.paths) {
                    drawPath(p, color = palette.border, style = Stroke(width = if (alert != null) 2.8f else 1.6f))
                }
            }

            // 4. Oblast name labels (zoom > 1.8)
            if (sc > 1.8f) {
                for (rp in oblastPaths) {
                    val c = regionCentroid(rp.region.rings) ?: continue
                    val p = project(c.first, c.second, frame, sc, off)
                    if (p.x in 0f..w && p.y in 0f..h)
                        drawContext.canvas.nativeCanvas.drawText(rp.region.name, p.x, p.y, oblastPaint)
                }
            }

            // 5. City dots + labels
            for (city in MAJOR_CITIES) {
                val p = project(city.lat, city.lon, frame, sc, off)
                if (p.x !in -40f..w + 40f || p.y !in -40f..h + 40f) continue
                // dot
                drawCircle(color = Color(palette.cityDot), radius = 4.5f, center = p)
                drawCircle(color = if (isDarkMap) Color.Black.copy(0.5f) else Color.White.copy(0.5f),
                    radius = 4.5f, center = p, style = Stroke(1f))
                // label offset to the right
                drawContext.canvas.nativeCanvas.drawText(
                    city.name, p.x + 8f, p.y + 5f, cityPaint)
            }

            // 6. Targets — white silhouettes on top
            val targetScale = (sc * 0.9f).coerceIn(0.7f, 2.2f)
            val silhouetteColor = targetSilhouetteColor("", isDarkMap)

            for (t in targets) {
                val p = project(t.lat, t.lng, frame, sc, off)
                if (p.x !in -60f..w + 60f || p.y !in -60f..h + 60f) continue

                when {
                    t.type.contains("FPV",      ignoreCase = true) ->
                        drawFpvSilhouette(p, t.bearing, silhouetteColor, targetScale)
                    t.type.contains("REACTIVE", ignoreCase = true) ->
                        drawAircraftSilhouette(p, t.bearing, silhouetteColor.copy(alpha = 0.85f), targetScale * 0.85f)
                    else -> // SHAHED and unknown
                        drawShahedSilhouette(p, t.bearing, silhouetteColor, targetScale)
                }
            }
        }

        // Reset zoom button — bottom-end corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 168.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f))
                .clickable {
                    scope.launch { scaleAnim.animateTo(MIN_SCALE) }
                    scope.launch { offsetAnim.animateTo(Offset.Zero) }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("↺", color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium)
        }
    }
}
