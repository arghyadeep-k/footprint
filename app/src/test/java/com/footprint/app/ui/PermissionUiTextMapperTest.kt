package com.footprint.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionUiTextMapperTest {

    @Test
    fun foregroundStatusLabel_mapsNotRequested() {
        assertEquals(
            "Foreground location not requested",
            PermissionUiTextMapper.foregroundStatusLabel(ForegroundPermissionStatus.NOT_REQUESTED)
        )
    }

    @Test
    fun foregroundStatusLabel_mapsDenied() {
        assertEquals(
            "Foreground location denied",
            PermissionUiTextMapper.foregroundStatusLabel(ForegroundPermissionStatus.DENIED)
        )
    }

    @Test
    fun foregroundStatusLabel_mapsPermanentlyDenied() {
        assertEquals(
            "Foreground location permanently denied",
            PermissionUiTextMapper.foregroundStatusLabel(ForegroundPermissionStatus.PERMANENTLY_DENIED)
        )
    }
}
