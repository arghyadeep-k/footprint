package com.footprint.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RetentionPolicyHelperTest {

    @Test
    fun keepForever_returnsNullCutoff() {
        val cutoff = RetentionPolicyHelper.cutoffEpochMillis(
            policy = RetentionPolicy.KEEP_FOREVER,
            nowEpochMillis = 1_700_000_000_000
        )

        assertNull(cutoff)
    }

    @Test
    fun deleteOlderThan30Days_returnsExpectedCutoff() {
        val now = 1_700_000_000_000L

        val cutoff = RetentionPolicyHelper.cutoffEpochMillis(
            policy = RetentionPolicy.DELETE_OLDER_THAN_30_DAYS,
            nowEpochMillis = now
        )

        assertEquals(now - (30L * 24 * 60 * 60 * 1000), cutoff)
    }

    @Test
    fun deleteOlderThan90Days_returnsExpectedCutoff() {
        val now = 1_700_000_000_000L

        val cutoff = RetentionPolicyHelper.cutoffEpochMillis(
            policy = RetentionPolicy.DELETE_OLDER_THAN_90_DAYS,
            nowEpochMillis = now
        )

        assertEquals(now - (90L * 24 * 60 * 60 * 1000), cutoff)
    }

    @Test
    fun deleteOlderThan1Year_returnsExpectedCutoff() {
        val now = 1_700_000_000_000L

        val cutoff = RetentionPolicyHelper.cutoffEpochMillis(
            policy = RetentionPolicy.DELETE_OLDER_THAN_1_YEAR,
            nowEpochMillis = now
        )

        assertEquals(now - (365L * 24 * 60 * 60 * 1000), cutoff)
    }
}
