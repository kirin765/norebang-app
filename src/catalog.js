// 곡 카탈로그 로드 + 검색 — 네이티브 CatalogDb.kt 검색 SQL을 그대로 재현
// public/songs.json (raw JSON): [[brand, number, title, artist], ...]
// norm_* 컬럼은 번들에서 제외하고 로드 시 JS 정규화로 재계산
// (130,599행 전수 비교로 네이티브 norm 컬럼과 일치 확인됨 — scripts/build-songs-json.mjs)

const STRIP = /[\s\p{P}·…〜~]+/gu // Kotlin \p{Punct} ≡ JS \p{P} (General_Category=Punctuation)

// TextNormalize.kt와 동일: 소문자 + 공백/문장부호 제거
export function normalizeQuery(s) {
  return s.toLowerCase().replace(STRIP, '')
}

let catalog = null // { meta:{version,count}, songs:[[brand,number,title,artist],...], normTitle:[], normArtist:[] }
let loadPromise = null

export function loadCatalog() {
  if (!loadPromise) {
    loadPromise = (async () => {
      const res = await fetch('songs.json', { cache: 'default' })
      if (!res.ok) throw new Error(`fetch songs.json -> ${res.status}`)
      const data = await res.json()
      const n = data.songs.length
      const normTitle = new Array(n)
      const normArtist = new Array(n)
      for (let i = 0; i < n; i++) {
        normTitle[i] = normalizeQuery(data.songs[i][2])
        normArtist[i] = normalizeQuery(data.songs[i][3])
      }
      catalog = { ...data, normTitle, normArtist }
      return catalog
    })()
  }
  return loadPromise
}

export function getCatalog() {
  return catalog
}

export function songCount() {
  return catalog ? catalog.meta.count : 0
}

export function dbVersion() {
  return catalog ? catalog.meta.version : ''
}

// CatalogDb.kt search()와 동일한 결과/순서:
// WHERE (norm_title LIKE %q% OR norm_artist LIKE %q%) [+ brand]
// ORDER BY (norm_title LIKE q%) DESC, (norm_artist LIKE q%) DESC, title, brand  LIMIT 300
export function search(rawQuery, brand, limit = 300) {
  if (!catalog) return []
  const q = normalizeQuery(rawQuery)
  if (!q) return []
  const out = []
  const { songs, normTitle, normArtist } = catalog
  for (let i = 0; i < songs.length; i++) {
    const [b, number, title, artist] = songs[i]
    if (brand && b !== brand) continue
    if (!normTitle[i].includes(q) && !normArtist[i].includes(q)) continue
    out.push({
      brand: b,
      number,
      title,
      artist,
      titlePrefix: normTitle[i].startsWith(q) ? 1 : 0,
      artistPrefix: normArtist[i].startsWith(q) ? 1 : 0,
    })
  }
  // 네이티브 ORDER BY 순서 재현: 제목 접두우선 > 가수 접두우선 > 제목 > 브랜드
  out.sort((a, b) => {
    if (a.titlePrefix !== b.titlePrefix) return b.titlePrefix - a.titlePrefix
    if (a.artistPrefix !== b.artistPrefix) return b.artistPrefix - a.artistPrefix
    const t = a.title.localeCompare(b.title, 'ko')
    if (t !== 0) return t
    return a.brand < b.brand ? -1 : a.brand > b.brand ? 1 : 0
  })
  return out.slice(0, limit)
}