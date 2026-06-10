package com.happylife2080.norebang.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritesDb(context: Context) : SQLiteOpenHelper(context, "user.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE favorites(
                brand TEXT NOT NULL,
                number INTEGER NOT NULL,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                added_at INTEGER NOT NULL,
                PRIMARY KEY(brand, number))"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun all(): List<Song> = readableDatabase.rawQuery(
        "SELECT brand, number, title, artist FROM favorites ORDER BY added_at DESC", null
    ).use { c ->
        buildList {
            while (c.moveToNext()) add(Song(c.getString(0), c.getInt(1), c.getString(2), c.getString(3)))
        }
    }

    fun keys(): Set<String> = readableDatabase.rawQuery(
        "SELECT brand, number FROM favorites", null
    ).use { c ->
        buildSet { while (c.moveToNext()) add("${c.getString(0)}:${c.getInt(1)}") }
    }

    fun add(song: Song) {
        writableDatabase.insertWithOnConflict(
            "favorites", null,
            ContentValues().apply {
                put("brand", song.brand)
                put("number", song.number)
                put("title", song.title)
                put("artist", song.artist)
                put("added_at", System.currentTimeMillis())
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun remove(song: Song) {
        writableDatabase.delete("favorites", "brand=? AND number=?", arrayOf(song.brand, song.number.toString()))
    }
}
