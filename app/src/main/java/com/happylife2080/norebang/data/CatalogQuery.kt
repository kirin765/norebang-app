package com.happylife2080.norebang.data

/** [CatalogDb.search]가 실행할 SQL과 바인딩 값. */
data class CatalogSearchQuery(
    val sql: String,
    val args: Array<String>,
)

/**
 * 곡 검색 SQL 생성기.
 *
 * - 곡명/가수명 부분 일치(기존 동작)
 * - 숫자만 입력하면 곡번호 검색 (앱 이름이 "번호검색"이므로 정확/접두 번호를 우선)
 * - TJ/금영 아티스트 표기 차이는 [ArtistAliases] 그룹으로 확장
 *
 * 순위: 번호 정확 > 번호 접두 > 제목 접두 > 가수(또는 별칭) 접두 > 제목 > 브랜드
 */
object CatalogQuery {

    private val NUMBER_QUERY = Regex("^\\d{1,7}$")

    fun isNumberQuery(normalized: String): Boolean = NUMBER_QUERY.matches(normalized)

    fun build(
        rawQuery: String,
        brand: String?,
        aliasGroups: Map<String, List<String>>,
        limit: Int = 300,
    ): CatalogSearchQuery? {
        val q = normalizeQuery(rawQuery)
        if (q.isEmpty()) return null

        val like = "%$q%"
        val prefix = "$q%"
        val aliases = ArtistAliases.targetsFor(q, aliasGroups)
        val numeric = isNumberQuery(q)

        val aliasWhere = aliases.joinToString("") { " OR norm_artist = ? OR norm_artist LIKE ?" }
        val numberWhere = if (numeric) " OR CAST(number AS TEXT) LIKE ?" else ""
        val brandWhere = if (brand != null) " AND brand = ?" else ""
        val numberOrder = if (numeric) {
            "(CAST(number AS TEXT) = ?) DESC, (CAST(number AS TEXT) LIKE ?) DESC, "
        } else {
            ""
        }
        val aliasRank = aliases.joinToString("") { " OR norm_artist LIKE ?" }

        val sql = """
            SELECT brand, number, title, artist FROM songs
            WHERE (norm_title LIKE ? OR norm_artist LIKE ?$numberWhere$aliasWhere)$brandWhere
            ORDER BY $numberOrder
              (norm_title LIKE ?) DESC,
              ((norm_artist LIKE ?)$aliasRank) DESC,
              title, brand
            LIMIT $limit
        """.trimIndent()

        val args = ArrayList<String>(8 + aliases.size * 3)
        args += like
        args += like
        if (numeric) args += "$q%"
        for (alias in aliases) {
            args += alias
            args += "$alias%"
        }
        if (brand != null) args += brand
        if (numeric) {
            args += q
            args += "$q%"
        }
        args += prefix
        args += prefix
        for (alias in aliases) args += "$alias%"

        return CatalogSearchQuery(sql, args.toTypedArray())
    }
}
