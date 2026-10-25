package com.happylife2080.norebang.data

/**
 * TJ ↔ 금영 아티스트 표기 통합.
 *
 * 데이터 파일(`assets/artist_aliases.tsv`)은 `scripts/build-songs-json.mjs`가
 * 카탈로그에서 자동 생성한다. 한 줄이 한 그룹이고 탭으로 구분한다.
 * 예: `iu\t아이유iu` — "아이유"로 검색해도 TJ의 "IU" 곡이 함께 나온다.
 *
 * 파일의 값은 이미 [normalizeQuery]를 거친 형태다.
 */
object ArtistAliases {

    fun parse(tsv: String): Map<String, List<String>> {
        val groups = HashMap<String, MutableList<String>>()
        tsv.lineSequence().forEach { rawLine ->
            val names = rawLine.trim()
                .split('\t')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
            if (names.size < 2) return@forEach
            for (name in names) {
                val others = groups.getOrPut(name) { mutableListOf() }
                for (other in names) {
                    if (other != name && other !in others) others.add(other)
                }
            }
        }
        return groups
    }

    /**
     * 정규화된 검색어로 별칭 그룹을 찾는다.
     *
     * - 그룹 구성원과 정확히 일치하면 확장한다.
     * - 3자 이상이면 구성원의 접두사여도 확장한다. (예: 금영 표기 "아이유iu"에
     *   대해 사용자가 "아이유"만 입력하는 경우)
     * - 1~2자 검색어는 너무 많은 그룹에 걸리므로 정확 일치만 허용한다.
     */
    fun targetsFor(normalizedQuery: String, groups: Map<String, List<String>>): List<String> {
        if (normalizedQuery.isEmpty()) return emptyList()
        val prefixAllowed = normalizedQuery.length >= 3
        val targets = LinkedHashSet<String>()
        for ((name, others) in groups) {
            if (name == normalizedQuery || (prefixAllowed && name.startsWith(normalizedQuery))) {
                targets += others
            }
        }
        targets.remove(normalizedQuery)
        return targets.toList()
    }
}
