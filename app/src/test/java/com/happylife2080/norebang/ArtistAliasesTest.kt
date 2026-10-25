package com.happylife2080.norebang

import com.happylife2080.norebang.data.ArtistAliases
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistAliasesTest {

    private val groups = ArtistAliases.parse(
        """
        iu	아이유iu
        twice	트와이스
        """.trimIndent()
    )

    @Test fun `parses both directions`() {
        assertEquals(listOf("아이유iu"), ArtistAliases.targetsFor("iu", groups))
        assertEquals(listOf("iu"), ArtistAliases.targetsFor("아이유iu", groups))
    }

    @Test fun `matches a prefix of a grouped name`() {
        assertEquals(listOf("iu"), ArtistAliases.targetsFor("아이유", groups))
        assertEquals(listOf("트와이스"), ArtistAliases.targetsFor("twi", groups))
    }

    @Test fun `returns empty for unknown or one-character query`() {
        assertEquals(emptyList<String>(), ArtistAliases.targetsFor("아", groups))
        assertEquals(emptyList<String>(), ArtistAliases.targetsFor("아이", groups))
        assertEquals(emptyList<String>(), ArtistAliases.targetsFor("bts", groups))
    }

    @Test fun `ignores malformed lines`() {
        val parsed = ArtistAliases.parse("lonely\n\na\tb\nb\ta")
        assertEquals(listOf("b"), ArtistAliases.targetsFor("a", parsed))
        assertTrue(ArtistAliases.parse("").isEmpty())
    }
}
