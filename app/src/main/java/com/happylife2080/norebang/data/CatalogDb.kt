package com.happylife2080.norebang.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

class CatalogDb(private val context: Context) {

    private var db: SQLiteDatabase? = null

    private val dbFile: File get() = File(context.filesDir, "catalog.db")

    @Synchronized
    fun open(): SQLiteDatabase {
        db?.let { if (it.isOpen) return it }
        if (!dbFile.exists()) copyFromAssets()
        val opened = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        db = opened
        return opened
    }

    private fun copyFromAssets() {
        context.assets.open("songs.db").use { input ->
            dbFile.outputStream().use { input.copyTo(it) }
        }
    }

    @Synchronized
    fun replaceWith(newDb: File) {
        db?.close()
        db = null
        newDb.copyTo(dbFile, overwrite = true)
        newDb.delete()
        open()
    }

    fun version(): String = open().rawQuery(
        "SELECT value FROM meta WHERE key='version'", null
    ).use { if (it.moveToFirst()) it.getString(0) else "?" }

    fun songCount(): Int = open().rawQuery(
        "SELECT COUNT(*) FROM songs", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun search(rawQuery: String, brand: String?, limit: Int = 300): List<Song> {
        val q = normalizeQuery(rawQuery)
        if (q.isEmpty()) return emptyList()
        val like = "%${q.replace("%", "").replace("_", "")}%"
        val brandClause = if (brand != null) "AND brand = ?" else ""
        val args = if (brand != null) arrayOf(like, like, brand, "$q%", "$q%") else arrayOf(like, like, "$q%", "$q%")
        val sql = """
            SELECT brand, number, title, artist FROM songs
            WHERE (norm_title LIKE ? OR norm_artist LIKE ?) $brandClause
            ORDER BY (norm_title LIKE ?) DESC, (norm_artist LIKE ?) DESC, title, brand
            LIMIT $limit
        """.trimIndent()
        return open().rawQuery(sql, args).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(Song(c.getString(0), c.getInt(1), c.getString(2), c.getString(3)))
                }
            }
        }
    }

    companion object {
        // sanity check used after downloading a new db before swapping it in
        fun isValidCatalog(file: File): Boolean = try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { d ->
                val count = d.rawQuery("SELECT COUNT(*) FROM songs", null)
                    .use { if (it.moveToFirst()) it.getInt(0) else 0 }
                val hasVersion = d.rawQuery("SELECT value FROM meta WHERE key='version'", null)
                    .use { it.moveToFirst() }
                count > 1000 && hasVersion
            }
        } catch (_: Exception) {
            false
        }
    }
}
