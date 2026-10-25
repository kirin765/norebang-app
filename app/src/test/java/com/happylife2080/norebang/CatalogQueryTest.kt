package com.happylife2080.norebang

import com.happylife2080.norebang.data.CatalogQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogQueryTest {

    @Test fun `blank query returns null`() {
        assertNull(CatalogQuery.build("   ", null, emptyMap()))
    }

    @Test fun `expands korean query to latin alias`() {
        val groups = mapOf("아이유iu" to listOf("iu"))
        val query = CatalogQuery.build("아이유", null, groups)!!
        assertTrue(query.sql.contains("norm_artist = ?"))
        assertTrue(query.sql.contains("OR norm_artist LIKE ?"))
        assertEquals(
            listOf("%아이유%", "%아이유%", "iu", "iu%", "아이유%", "아이유%", "iu%"),
            query.args.toList(),
        )
    }

    @Test fun `number query ranks song number first`() {
        val query = CatalogQuery.build("95118", "TJ", emptyMap())!!
        assertTrue(query.sql.contains("CAST(number AS TEXT) = ?"))
        assertTrue(query.sql.contains("CAST(number AS TEXT) LIKE ?"))
        assertTrue(query.sql.contains("AND brand = ?"))
        assertEquals(
            listOf("%95118%", "%95118%", "95118%", "TJ", "95118", "95118%", "95118%", "95118%"),
            query.args.toList(),
        )
    }

    @Test fun `text query does not add number ordering`() {
        val query = CatalogQuery.build(" 밤 편지! ", null, emptyMap())!!
        assertFalse(query.sql.contains("CAST(number"))
        assertEquals("%밤편지%", query.args[0])
        assertEquals("%밤편지%", query.args[1])
        assertEquals("밤편지%", query.args[2])
    }

    @Test fun `eight digit query is not a song number`() {
        assertFalse(CatalogQuery.isNumberQuery("12345678"))
        assertTrue(CatalogQuery.isNumberQuery("1234"))
    }

    @Test fun `placeholder count always matches bound args`() {
        val groups = mapOf("아이유iu" to listOf("iu"), "twice" to listOf("트와이스"))
        val cases = listOf(
            Triple("아이유", null, groups),
            Triple("아이유", "TJ", groups),
            Triple("95118", null, emptyMap()),
            Triple("95118", "KY", emptyMap()),
            Triple("밤 편지", null, emptyMap()),
            Triple("1730", null, mapOf("1730" to listOf("일칠삼공"))),
        )
        for ((q, brand, aliases) in cases) {
            val built = CatalogQuery.build(q, brand, aliases)!!
            assertEquals("$q/$brand", built.sql.count { it == '?' }, built.args.size)
        }
    }
}
