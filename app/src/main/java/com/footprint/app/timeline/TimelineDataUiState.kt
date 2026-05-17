package com.footprint.app.timeline

import com.footprint.app.data.local.LocationPoint

sealed interface TimelineDataUiState {
    data object Loading : TimelineDataUiState
    data object Empty : TimelineDataUiState
    data class Success(val points: List<LocationPoint>) : TimelineDataUiState
    data class Error(val message: String) : TimelineDataUiState
}

object TimelineDataUiStateFactory {
    fun fromPoints(points: List<LocationPoint>): TimelineDataUiState {
        return if (points.isEmpty()) {
            TimelineDataUiState.Empty
        } else {
            TimelineDataUiState.Success(points)
        }
    }

    fun fromError(error: Throwable): TimelineDataUiState {
        val message = error.message?.takeIf { it.isNotBlank() }
            ?: "Could not load travel history right now."
        return TimelineDataUiState.Error(message)
    }
}
