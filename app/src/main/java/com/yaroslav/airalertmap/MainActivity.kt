package com.airalertmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.Threat
import com.airalertmap.data.model.ThreatType
import com.airalertmap.ui.MapViewModel
import com.airalertmap.ui.ThreatMap
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
    val allThreats by viewModel.threats.collectAsState()
    val alertOblasts by viewModel.alertOblasts.collectAsState()
    val alertRaions by viewModel.alertRaions.collectAsState()
    val oblastPolygons by viewModel.oblastPolygons.collectAsState()
    val raionPolygons by viewModel.raionPolygons.collectAsState()
    val isDarkMap by viewModel.isDarkMap.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedType by remember { mutableStateOf<ThreatType?>(null) }
    var selectedThreat by remember { mutableStateOf<Threat?>(null) }
    var showAlerts by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    val threats = if (selectedType == null) {
        allThreats
    } else {
        allThreats.filter { it.type == selectedType }
    }

    Box(Modifier.fillMaxSize()) {
        ThreatMap(
            threats = threats,
            oblastPolygons = oblastPolygons,
            raionPolygons = raionPolygons,
            alertOblasts = alertOblasts,
            alertRaions = alertRaions,
            isDarkMap = isDarkMap,
            selectedThreatId = selectedThreat?.id,
            onThreatClick = { selectedThreat = it },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE8171D24))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "AIR ALERT MAP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (error == null) stringResource(R.string.status_updating) else stringResource(R.string.status_offline),
                            color = if (error == null) Color(0xFF81C784) else Color(0xFFFFB74D),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconAction(if (isDarkMap) "☀" else "☾") { viewModel.toggleMapTheme() }
                    Spacer(Modifier.width(6.dp))
                    IconAction("⚙") { showSettings = true }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        modifier = Modifier.weight(1f),
                        value = allThreats.size.toString(),
                        label = stringResource(R.string.targets),
                        color = Color(0xFFFFB74D)
                    )
                    StatBadge(
                        modifier = Modifier.weight(1f),
                        value = (alertOblasts.size + alertRaions.size).toString(),
                        label = stringResource(R.string.alerts),
                        color = if (alertOblasts.isNotEmpty() || alertRaions.isNotEmpty()) Color(0xFFE0574A) else Color(0xFF81C784)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(stringResource(R.string.all), selectedType == null) { selectedType = null }
                    FilterChip(stringResource(R.string.uav), selectedType == ThreatType.UAV) { selectedType = ThreatType.UAV }
                    FilterChip(stringResource(R.string.fpv), selectedType == ThreatType.FPV) { selectedType = ThreatType.FPV }
                    FilterChip(stringResource(R.string.missile), selectedType == ThreatType.MISSILE) { selectedType = ThreatType.MISSILE }
                    FilterChip(stringResource(R.string.ballistic), selectedType == ThreatType.BALLISTIC) { selectedType = ThreatType.BALLISTIC }
                    FilterChip(stringResource(R.string.kab), selectedType == ThreatType.KAB) { selectedType = ThreatType.KAB }
                    FilterChip(stringResource(R.string.mig31k), selectedType == ThreatType.MIG31K) { selectedType = ThreatType.MIG31K }
                    FilterChip(stringResource(R.string.recon), selectedType == ThreatType.RECON) { selectedType = ThreatType.RECON }
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEC11161D))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.official_alerts),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.regions_count, alertOblasts.size, alertRaions.size),
                            color = Color(0xFFB0BEC5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = { showAlerts = !showAlerts }) {
                        Text(if (showAlerts) stringResource(R.string.hide) else stringResource(R.string.show))
                    }
                }

                if (showAlerts && (alertOblasts.isNotEmpty() || alertRaions.isNotEmpty())) {
                    HorizontalDivider(color = Color(0xFF29313A), modifier = Modifier.padding(vertical = 8.dp))

                    alertOblasts.take(6).forEach { AlertRow(it, true) }
                    alertRaions.take(6).forEach { AlertRow(it, false) }

                    val remaining =
                        (alertOblasts.size - 6).coerceAtLeast(0) +
                            (alertRaions.size - 6).coerceAtLeast(0)

                    if (remaining > 0) {
                        Text(
                            stringResource(R.string.more, remaining),
                            color = Color(0xFF90CAF9),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else if (showAlerts) {
                    Text(
                        stringResource(R.string.no_alerts),
                        color = Color(0xFF81C784),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(4.dp))

                val context = LocalContext.current
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://neptun.in.ua/")
                            )
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.neptun_data))
                }
            }
        }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }

        selectedThreat?.let { threat ->
            ThreatDetailsDialog(
                threat = threat,
                onDismiss = { selectedThreat = null }
            )
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ThreatDetailsDialog(
    threat: Threat,
    onDismiss: () -> Unit
) {
    val typeLabel = when (threat.type) {
        ThreatType.UAV -> stringResource(R.string.uav)
        ThreatType.FPV -> stringResource(R.string.fpv)
        ThreatType.RECON -> stringResource(R.string.recon)
        ThreatType.MISSILE -> stringResource(R.string.missile)
        ThreatType.BALLISTIC -> stringResource(R.string.ballistic)
        ThreatType.KAB -> stringResource(R.string.kab)
        ThreatType.MIG31K -> stringResource(R.string.mig31k)
        ThreatType.UNKNOWN -> stringResource(R.string.unknown_type)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(threat.title.ifBlank { stringResource(R.string.target_details) }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailLine(stringResource(R.string.target_type), typeLabel)
                DetailLine(stringResource(R.string.target_region), threat.region ?: "—")
                DetailLine(stringResource(R.string.target_district), threat.district ?: "—")
                DetailLine(stringResource(R.string.target_locality), threat.locality ?: "—")
                DetailLine(stringResource(R.string.target_status), threat.status.apiValue)
                DetailLine(stringResource(R.string.target_coordinates), "%.4f, %.4f".format(threat.lat, threat.lon))
                threat.heading?.let { DetailLine(stringResource(R.string.target_heading), "%.0f°".format(it)) }
                threat.velocity?.speedKmh?.let { DetailLine(stringResource(R.string.target_speed), "%.0f km/h".format(it)) }
                threat.uncertaintyKm?.let { DetailLine(stringResource(R.string.target_uncertainty), "%.1f km".format(it)) }
                threat.updatedAt.takeIf { it.isNotBlank() }?.let { DetailLine(stringResource(R.string.target_updated), it) }
                threat.explanationShort?.takeIf { it.isNotBlank() }?.let { DetailLine(stringResource(R.string.target_note), it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF90A4AE), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.project_section),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { openUrl("https://fan-world-me.github.io/airalertmap/") }) {
                    Text(stringResource(R.string.project_website))
                }
                TextButton(onClick = { openUrl("https://github.com/fan-world-me/airalertmap") }) {
                    Text(stringResource(R.string.github_project))
                }
                TextButton(onClick = { openUrl("https://github.com/fan-world-me/airalertmap/releases/latest") }) {
                    Text(stringResource(R.string.latest_release))
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    stringResource(R.string.support_section),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.support_description),
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = { openUrl("https://github.com/sponsors/fan-world-me") }) {
                    Text(stringResource(R.string.github_sponsors))
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.version_info),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun IconAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222A33))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun StatBadge(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222A33))
            .padding(vertical = 8.dp)
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFB0BEC5), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFF263238) else Color(0xFF171D24),
            labelColor = if (selected) Color.White else Color(0xFFB0BEC5)
        )
    )
}

@Composable
private fun AlertRow(area: AlertArea, oblast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFF5252))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = area.name,
            color = Color.White,
            fontWeight = if (oblast) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
