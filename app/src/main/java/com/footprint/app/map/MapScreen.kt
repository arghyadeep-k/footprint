package com.footprint.app.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.footprint.app.data.local.LocationPoint
import com.footprint.app.timeline.TimelineRange
import com.footprint.app.timeline.TimelineDataUiState
import com.footprint.app.timeline.VisitSegment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MapScreen(
    timelineState: TimelineDataUiState,
    points: List<LocationPoint>,
    visitSegments: List<VisitSegment>,
    selectedVisitIndex: Int?,
    selectedRange: TimelineRange,
    onRetry: () -> Unit,
    onVisitSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timelineState is TimelineDataUiState.Empty) {
        if (points.isEmpty()) {
            EmptyMapState(modifier)
            return
        }
    }

    if (timelineState is TimelineDataUiState.Error) {
        ErrorMapState(
            message = timelineState.message,
            onRetry = onRetry,
            modifier = modifier
        )
        return
    }

    if (timelineState is TimelineDataUiState.Loading && points.isEmpty()) {
        LoadingMapState(modifier)
        return
    }

    val cameraPositionState = rememberCameraPositionState()
    val routePoints = remember(points, selectedRange, timelineState) {
        val sourcePoints = when (timelineState) {
            is TimelineDataUiState.Success -> timelineState.points
            else -> points
        }
        val latLngPoints = sourcePoints.map { LatLng(it.latitude, it.longitude) }
        PolylineThinner.thin(
            points = latLngPoints,
            minDistanceMeters = thinningDistanceForRange(selectedRange),
            maxPoints = 1200
        )
    }
    val routeMarkers = remember(routePoints, visitSegments) {
        RouteMarkerPlanner.plan(
            routePoints = routePoints,
            visitSegments = visitSegments
        )
    }

    LaunchedEffect(routePoints) {
        fitCameraToRoute(routePoints, cameraPositionState)
    }
    LaunchedEffect(selectedVisitIndex, visitSegments) {
        val selected = selectedVisitIndex?.takeIf { it in visitSegments.indices } ?: return@LaunchedEffect
        val visit = visitSegments[selected]
        focusCameraOnVisit(visit.latitude, visit.longitude, cameraPositionState)
    }

    if (routePoints.isEmpty()) {
        EmptyMapState(modifier)
        return
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        Polyline(
            points = routePoints,
            color = MaterialTheme.colorScheme.primary,
            width = 8f
        )

        routeMarkers.start?.let { start ->
            Marker(
                state = MarkerState(position = start),
                title = "Start",
                snippet = "Route start"
            )
        }

        routeMarkers.end?.let { end ->
            Marker(
                state = MarkerState(position = end),
                title = "End",
                snippet = "Route end"
            )
        }

        visitSegments.forEachIndexed { index, visit ->
            Marker(
                state = MarkerState(position = LatLng(visit.latitude, visit.longitude)),
                title = if (selectedVisitIndex == index) "Place ${index + 1} (Selected)" else "Place ${index + 1}",
                snippet = "${visit.pointCount} points",
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (selectedVisitIndex == index) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                ),
                onClick = {
                    onVisitSelected(index)
                    false
                }
            )
        }
    }

    if (timelineState is TimelineDataUiState.Loading && points.isNotEmpty()) {
        LoadingOverlay(modifier)
    }
}

@Composable
private fun LoadingMapState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading travel map...")
        }
    }
}

@Composable
private fun EmptyMapState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No travel points for this timeline yet.",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Start tracking to draw your travel line on the map.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorMapState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Could not load map data.",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
                Text("Refreshing map…")
            }
        }
    }
}

private suspend fun fitCameraToRoute(
    routePoints: List<LatLng>,
    cameraPositionState: CameraPositionState
) {
    if (routePoints.isEmpty()) return

    if (routePoints.size == 1) {
        withContext(Dispatchers.Main) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(routePoints.first(), 15f),
                durationMs = 700
            )
        }
        return
    }

    val boundsBuilder = LatLngBounds.Builder()
    routePoints.forEach { boundsBuilder.include(it) }
    val bounds = boundsBuilder.build()

    withContext(Dispatchers.Main) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(bounds, 120),
            durationMs = 700
        )
    }
}

private suspend fun focusCameraOnVisit(
    latitude: Double,
    longitude: Double,
    cameraPositionState: CameraPositionState
) {
    withContext(Dispatchers.Main) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f),
            durationMs = 600
        )
    }
}

private fun thinningDistanceForRange(range: TimelineRange): Double {
    return when (range) {
        TimelineRange.Today -> 15.0
        TimelineRange.Yesterday -> 20.0
        TimelineRange.ThisWeek -> 35.0
        TimelineRange.ThisMonth -> 60.0
        TimelineRange.ThisYear -> 120.0
        TimelineRange.Last24Hours -> 20.0
        TimelineRange.Last7Days -> 40.0
        TimelineRange.Last30Days -> 75.0
        TimelineRange.Lifetime -> 180.0
        is TimelineRange.Custom -> 50.0
    }
}
