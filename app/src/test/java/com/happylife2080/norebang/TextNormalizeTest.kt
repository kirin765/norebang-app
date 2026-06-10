package com.happylife2080.norebang

import com.happylife2080.norebang.data.normalizeQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizeTest {
    @Test fun `lowercases latin`() = assertEquals("dynamite", normalizeQuery("Dynamite"))
    @Test fun `strips whitespace`() = assertEquals("밤편지", normalizeQuery("밤 편지"))
    @Test fun `strips punctuation`() = assertEquals("어머나", normalizeQuery("어머나!"))
    @Test fun `keeps hangul intact`() = assertEquals("아이유", normalizeQuery("아이유"))
    @Test fun `mixed case and spaces`() = assertEquals("lovedive", normalizeQuery("LOVE DIVE"))
    @Test fun `empty stays empty`() = assertEquals("", normalizeQuery("  "))
    @Test fun `parens and dots stripped`() = assertEquals("취중진담feat", normalizeQuery("취중진담 (Feat.)"))
}
