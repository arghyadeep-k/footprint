package com.footprint.app.location

data class TrackingState(
    val status: Status,
    val effectiveMode: TrackingMode? = null,
    val isActiveTripRunning: Boolean = false,
    val errorMessage: String? = null
) {
    enum class Status {
        STOPPED,
        RUNNING,
        PERMISSION_MISSING,
        ERROR
    }

    companion object {
        fun stopped(): TrackingState = TrackingState(status = Status.STOPPED)
    }
}
