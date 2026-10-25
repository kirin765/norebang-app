package com.happylife2080.norebang.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.IOException

class CatalogDb(private val context: Context) {

    private var db: SQLiteDatabase? = null

    private val dbFile: File get() = File(context.filesDir, "catalog.db")
    private val backupFile: File get() = File(context.filesDir, "catalog.db.bak")
    private val stagedFile: File get() = File(context.filesDir, "catalog.db.staged")

    /** 카탈로그에서 자동 생성된 TJ↔금영 아티스트 별칭. 없으면 빈 맵(검색은 계속 동작). */
    private val aliasGroups: Map<String, List<String>> by lazy {
        try {
            context.assets.open("artist_aliases.tsv").bufferedReader().use {
                ArtistAliases.parse(it.readText())
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /**
     * DB를 연다. 파일이 없거나 손상됐으면 자산의 원본으로 복구한다.
     * 모든 공개 메서드가 이 락을 공유하므로 업데이트 도중 쿼리가 끼어들 수 없다.
     */
    @Synchronized
    fun open(): SQLiteDatabase {
        db?.let { if (it.isOpen) return it }
        if (!dbFile.exists()) restoreBaseCatalog()
        return try {
            openExisting()
        } catch (_: Exception) {
            // 중간에 끊긴 교체/복사로 깨진 파일이면 원본으로 되돌린다.
            // 백업까지 깨졌을 수 있으므로 자산 원본을 바로 사용한다.
            dbFile.delete()
            backupFile.delete()
            copyFromAssets()
            openExisting()
        }
    }

    private fun openExisting(): SQLiteDatabase =
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            .also { db = it }

    private fun restoreBaseCatalog() {
        // 교체가 중간에 실패해 백업만 남은 경우 백업을 우선 복원한다.
        if (backupFile.exists() && backupFile.renameTo(dbFile)) return
        copyFromAssets()
    }

    private fun copyFromAssets() {
        context.assets.open("songs.db").use { input ->
            dbFile.outputStream().use { input.copyTo(it) }
        }
    }

    /**
     * 새 카탈로그를 원자적으로 교체한다.
     *
     * 기존 구현은 열린 파일 위에 바로 덮어써서, 복사가 중간에 실패하면 DB가
     * 영구적으로 깨질 수 있었다. 이제는 (1) 임시 파일로 복사·검증, (2) 기존 DB를
     * 백업으로 이동, (3) 임시 파일을 제자리로 rename 순서로 진행한다.
     */
    @Synchronized
    fun replaceWith(newDb: File) {
        val staged = stagedFile
        if (staged.exists()) staged.delete()
        newDb.copyTo(staged, overwrite = true)

        if (!isValidCatalog(staged)) {
            staged.delete()
            newDb.delete()
            throw IOException("downloaded catalog failed validation")
        }

        db?.close()
        db = null

        if (backupFile.exists()) backupFile.delete()
        if (dbFile.exists() && !dbFile.renameTo(backupFile)) {
            staged.delete()
            newDb.delete()
            open()
            throw IOException("could not move current catalog aside")
        }
        if (!staged.renameTo(dbFile)) {
            backupFile.renameTo(dbFile)
            staged.delete()
            newDb.delete()
            open()
            throw IOException("could not install new catalog")
        }

        backupFile.delete()
        newDb.delete()
        open()
    }

    @Synchronized
    fun version(): String = open().rawQuery(
        "SELECT value FROM meta WHERE key='version'", null
    ).use { if (it.moveToFirst()) it.getString(0) else "?" }

    @Synchronized
    fun songCount(): Int = open().rawQuery(
        "SELECT COUNT(*) FROM songs", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    @Synchronized
    fun search(rawQuery: String, brand: String?, limit: Int = 300): List<Song> {
        val query = CatalogQuery.build(rawQuery, brand, aliasGroups, limit) ?: return emptyList()
        return open().rawQuery(query.sql, query.args).use { c ->
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
