package com.airalertmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.DimapTarget
import com.airalertmap.ui.AlertMap
import com.airalertmap.ui.MapViewModel
import com.airalertmap.ui.theme.AirAlertTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AirAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AirAlertApp(viewModel)
                }
            }
        }
    }
}

@Composable
private fun AirAlertApp(viewModel: MapViewModel) {
    val alertOblasts   by viewModel.alertOblasts.collectAsState()
    val alertRaions    by viewModel.alertRaions.collectAsState()
    val oblastPolygons by viewModel.oblastPolygons.collectAsState()
    val raionPolygons  by viewModel.raionPolygons.collectAsState()
    val targets        by viewModel.targets.collectAsState()
    val isDarkMap      by viewModel.isDarkMap.collectAsState()
    val loading        by viewModel.isLoading.collectAsState()
    val error          by viewModel.error.collectAsState()

    var showAlerts   by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    val isOnline = error == null

    Box(Modifier.fillMaxSize()) {

        // ── Map ──────────────────────────────────────────────────────────────
        AlertMap(
            oblastPolygons = oblastPolygons,
            raionPolygons  = raionPolygons,
            alertOblasts   = alertOblasts,
            alertRaions    = alertRaions,
            targets        = targets,
            isDarkMap      = isDarkMap,
            modifier       = Modifier.fillMaxSize()
        )

        // ── Top header card ──────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // App title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status dot + title
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dotAlpha by animateFloatAsState(
                                targetValue = if (loading) 0.4f else 1f,
                                animationSpec = tween(600), label = "dot"
                            )
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .alpha(dotAlpha)
                                    .background(
                                        if (isOnline) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.error
                                    )
                            )
                            Text(
                                text = if (isOnline) stringResource(R.string.status_updating)
                                       else stringResource(R.string.status_offline),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Theme toggle
                    FilledTonalButton(
                        onClick = { viewModel.toggleMapTheme() },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = if (isDarkMap) "☀" else "🌙",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Settings
                    FilledTonalButton(
                        onClick = { showSettings = true },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("ℹ", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Stat chips row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip(
                        modifier  = Modifier.weight(1f),
                        value     = alertOblasts.size.toString(),
                        label     = stringResource(R.string.oblasts),
                        tint      = MaterialTheme.colorScheme.error
                    )
                    StatChip(
                        modifier  = Modifier.weight(1f),
                        value     = alertRaions.size.toString(),
                        label     = stringResource(R.string.raions),
                        tint      = MaterialTheme.colorScheme.secondary
                    )
                    StatChip(
                        modifier  = Modifier.weight(1f),
                        value     = targets.size.toString(),
                        label     = "цілей",
                        tint      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Bottom sheet card ────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section header
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text  = stringResource(R.string.official_alerts),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text  = stringResource(R.string.regions_count, alertOblasts.size, alertRaions.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showAlerts = !showAlerts }) {
                        Text(
                            text  = if (showAlerts) stringResource(R.string.hide) else stringResource(R.string.show),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showAlerts,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    val alerts = alertOblasts + alertRaions
                    if (alerts.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text("✓", color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                text  = stringResource(R.string.no_alerts),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LazyColumn(
                                modifier = Modifier.height((minOf(alerts.size, 4) * 50).dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                items(alerts.take(12), key = { it.key }) { AlertRow(it) }
                            }
                            if (alerts.size > 12) {
                                Text(
                                    text  = stringResource(R.string.more, alerts.size - 12),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                val context = LocalContext.current
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://api.dimap.live/"))
                        )
                    }) {
                        Text(
                            text  = stringResource(R.string.provider_data),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // ── Settings dialog ──────────────────────────────────────────────────
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        tint.copy(alpha = 0.18f),
                        tint.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = value,
            color = tint,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text  = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

// ── Alert list row ────────────────────────────────────────────────────────────
@Composable
private fun AlertRow(alert: AlertArea) {
    val isWarning = alert.color?.lowercase()?.let {
        it == "yellow" || it == "warning"
    } == true

    val accentColor = if (isWarning) Color(0xFFFFB84D) else MaterialTheme.colorScheme.error
    val bgColor     = if (isWarning)
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.30f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Text(
            text  = alert.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (isWarning) {
            Text("⚠", color = accentColor, style = MaterialTheme.typography.labelMedium)
        } else {
            Text("🚨", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── Settings dialog ───────────────────────────────────────────────────────────
@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) =
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text  = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = stringResource(R.string.project_section),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                TextButton(
                    onClick = { openUrl("https://fan-world-me.github.io/airalertmap/") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = stringResource(R.string.project_website),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = { openUrl("https://github.com/fan-world-me/airalertmap") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = stringResource(R.string.github_project),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = { openUrl("https://github.com/fan-world-me/airalertmap/releases/latest") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = stringResource(R.string.latest_release),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text  = stringResource(R.string.version_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
