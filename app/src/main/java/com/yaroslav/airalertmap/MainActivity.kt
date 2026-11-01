package com.airalertmap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airalertmap.data.model.AlertArea
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
    val alertOblasts by viewModel.alertOblasts.collectAsState()
    val alertRaions by viewModel.alertRaions.collectAsState()
    val oblastPolygons by viewModel.oblastPolygons.collectAsState()
    val raionPolygons by viewModel.raionPolygons.collectAsState()
    val isDarkMap by viewModel.isDarkMap.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAlerts by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AlertMap(
            oblastPolygons = oblastPolygons,
            raionPolygons = raionPolygons,
            alertOblasts = alertOblasts,
            alertRaions = alertRaions,
            isDarkMap = isDarkMap,
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.app_name),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (error == null) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.error
                                    )
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                if (error == null) stringResource(R.string.status_updating)
                                else stringResource(R.string.status_offline),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.toggleMapTheme() },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(if (isDarkMap) "☼" else "☾", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(6.dp))
                    FilledTonalButton(
                        onClick = { showSettings = true },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("⋯", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        value = alertOblasts.size.toString(),
                        label = stringResource(R.string.oblasts),
                        tint = MaterialTheme.colorScheme.error
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        value = alertRaions.size.toString(),
                        label = stringResource(R.string.raions),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.official_alerts),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.regions_count, alertOblasts.size, alertRaions.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = { showAlerts = !showAlerts }) {
                        Text(if (showAlerts) stringResource(R.string.hide) else stringResource(R.string.show))
                    }
                }

                if (showAlerts) {
                    val alerts = alertOblasts + alertRaions
                    if (alerts.isEmpty()) {
                        Text(
                            stringResource(R.string.no_alerts),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height((minOf(alerts.size, 4) * 42).dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(alerts.take(12), key = { it.key }) { alert ->
                                AlertRow(alert)
                            }
                        }
                        if (alerts.size > 12) {
                            Text(
                                stringResource(R.string.more, alerts.size - 12),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                val context = LocalContext.current
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://api.dimap.live/"))
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.provider_data))
                }
            }
        }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(42.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(value, color = tint, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AlertRow(alert: AlertArea) {
    val tint = when (alert.color?.lowercase()) {
        "yellow", "warning" -> Color(0xFFFFC047)
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(tint)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = alert.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
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
                Text(stringResource(R.string.project_section), fontWeight = FontWeight.SemiBold)
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
                Text(stringResource(R.string.support_section), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.support_description), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { openUrl("https://github.com/sponsors/fan-world-me") }) {
                    Text(stringResource(R.string.github_sponsors))
                }
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.version_info), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
