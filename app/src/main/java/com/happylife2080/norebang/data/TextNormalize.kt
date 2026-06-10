package com.happylife2080.norebang.data

private val STRIP = Regex("[\\s\\p{Punct}·…〜~]+")

fun normalizeQuery(s: String): String = s.lowercase().replace(STRIP, "")
