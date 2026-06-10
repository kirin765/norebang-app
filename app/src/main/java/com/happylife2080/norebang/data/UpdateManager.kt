package com.happylife2080.norebang.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class UpdateManager(private val context: Context, private val catalog: CatalogDb) {

    private val prefs = context.getSharedPreferences("update", Context.MODE_PRIVATE)

    suspend fun checkAndUpdate(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!force && !UpdatePolicy.shouldCheck(prefs.getLong("last_check", 0L), now)) return@withContext false
        try {
            val json = fetch(VERSION_URL).toString(Charsets.UTF_8)
            val (remoteVersion, dbUrl) = UpdatePolicy.parseVersionJson(json) ?: return@withContext false
            prefs.edit().putLong("last_check", now).apply()
            if (!UpdatePolicy.isNewer(remoteVersion, catalog.version())) return@withContext false

            val tmp = File(context.cacheDir, "catalog-new.db")
            URL(dbUrl).openConnection().let { conn ->
                (conn as HttpURLConnection).connectTimeout = 15000
                conn.readTimeout = 60000
                GZIPInputStream(conn.inputStream).use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                }
            }
            if (!CatalogDb.isValidCatalog(tmp)) {
                tmp.delete()
                return@withContext false
            }
            catalog.replaceWith(tmp)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun fetch(url: String): ByteArray {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        return conn.inputStream.use { it.readBytes() }
    }

    companion object {
        const val VERSION_URL = "https://raw.githubusercontent.com/kirin765/norebang-data/main/version.json"
    }
}
