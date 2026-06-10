package com.happylife2080.norebang

import com.happylife2080.norebang.data.UpdatePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {

    @Test fun `first launch should check`() =
        assertTrue(UpdatePolicy.shouldCheck(lastCheckMs = 0, nowMs = System.currentTimeMillis()))

    @Test fun `recent check skips`() =
        assertFalse(UpdatePolicy.shouldCheck(lastCheckMs = 1000, nowMs = 1000 + 60_000))

    @Test fun `week elapsed checks`() =
        assertTrue(UpdatePolicy.shouldCheck(lastCheckMs = 0, nowMs = UpdatePolicy.CHECK_INTERVAL_MS))

    @Test fun `newer date version wins`() {
        assertTrue(UpdatePolicy.isNewer("2026-07-01", "2026-06-10"))
        assertFalse(UpdatePolicy.isNewer("2026-06-10", "2026-06-10"))
        assertFalse(UpdatePolicy.isNewer("2026-05-01", "2026-06-10"))
        assertFalse(UpdatePolicy.isNewer("", "2026-06-10"))
    }

    @Test fun `parses version json`() {
        val parsed = UpdatePolicy.parseVersionJson(
            """{"version": "2026-06-10", "db_url": "https://example.com/songs.db.gz", "songs": 123}"""
        )
        assertEquals("2026-06-10" to "https://example.com/songs.db.gz", parsed)
    }

    @Test fun `rejects malformed json`() {
        assertNull(UpdatePolicy.parseVersionJson("{}"))
        assertNull(UpdatePolicy.parseVersionJson("""{"version":"2026-06-10"}"""))
    }
}
