package com.airalertmap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.airalertmap.data.AlertsRepository
import com.airalertmap.data.GeoJsonLoader
import com.airalertmap.data.model.AlertArea
import com.airalertmap.data.model.DimapTarget
import com.airalertmap.data.model.RegionPolygon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository    = AlertsRepository()
    private val geoJsonLoader = GeoJsonLoader(application.applicationContext)

    private val _alertOblasts = MutableStateFlow<List<AlertArea>>(emptyList())
    val alertOblasts: StateFlow<List<AlertArea>> = _alertOblasts.asStateFlow()

    private val _alertRaions = MutableStateFlow<List<AlertArea>>(emptyList())
    val alertRaions: StateFlow<List<AlertArea>> = _alertRaions.asStateFlow()

    private val _targets = MutableStateFlow<List<DimapTarget>>(emptyList())
    val targets: StateFlow<List<DimapTarget>> = _targets.asStateFlow()

    private val _serverTime = MutableStateFlow<String?>(null)
    val serverTime: StateFlow<String?> = _serverTime.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastSuccessfulUpdate = MutableStateFlow<Long?>(null)
    val lastSuccessfulUpdate: StateFlow<Long?> = _lastSuccessfulUpdate.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _oblastPolygons = MutableStateFlow<List<RegionPolygon>>(emptyList())
    val oblastPolygons: StateFlow<List<RegionPolygon>> = _oblastPolygons.asStateFlow()

    private val _raionPolygons = MutableStateFlow<List<RegionPolygon>>(emptyList())
    val raionPolygons: StateFlow<List<RegionPolygon>> = _raionPolygons.asStateFlow()

    private val _isDarkMap = MutableStateFlow(true)
    val isDarkMap: StateFlow<Boolean> = _isDarkMap.asStateFlow()

    fun toggleMapTheme() { _isDarkMap.value = !_isDarkMap.value }

    init {
        // GeoJSON loaded from bundled assets — no network, instant
        viewModelScope.launch { _oblastPolygons.value = geoJsonLoader.loadOblasts() }
        viewModelScope.launch { _raionPolygons.value  = geoJsonLoader.loadRaions()  }

        viewModelScope.launch {
            repository.observeSnapshot().collect { result ->
                result.onSuccess { snapshot ->
                    _alertOblasts.value          = snapshot.alertOblasts
                    _alertRaions.value           = snapshot.alertRaions
                    _targets.value               = snapshot.targets
                    _serverTime.value            = snapshot.serverTime
                    _lastSuccessfulUpdate.value  = System.currentTimeMillis()
                    _error.value                 = null
                    _isLoading.value             = false
                }.onFailure { t ->
                    _error.value     = t.message ?: "Немає з'єднання"
                    _isLoading.value = false
                }
            }
        }
    }
}
