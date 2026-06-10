package com.happylife2080.norebang.data

object UpdatePolicy {
    const val CHECK_INTERVAL_MS: Long = 7L * 24 * 60 * 60 * 1000 // weekly check; DB itself changes monthly

    fun shouldCheck(lastCheckMs: Long, nowMs: Long): Boolean =
        nowMs - lastCheckMs >= CHECK_INTERVAL_MS

    // versions are "YYYY-MM-DD" strings, so lexicographic compare is chronological
    fun isNewer(remote: String, local: String): Boolean =
        remote.isNotBlank() && remote > local

    fun parseVersionJson(json: String): Pair<String, String>? {
        val version = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: return null
        val dbUrl = Regex("\"db_url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: return null
        return version to dbUrl
    }
}
