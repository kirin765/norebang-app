// 곡 카탈로그 로드 + 검색 — 네이티브 CatalogQuery.kt와 같은 규칙
// public/songs.json (raw JSON): [[brand, number, title, artist], ...]
// public/artist_aliases.tsv: TJ↔금영 아티스트 표기 통합 그룹(탭 구분)
// norm_* 컬럼은 번들에서 제외하고 로드 시 JS 정규화로 재계산
// (130,599행 전수 비교로 네이티브 norm 컬럼과 일치 확인됨 — scripts/build-songs-json.mjs)

const STRIP = /[\s\p{P}·…〜~]+/gu // Kotlin \p{Punct} ≡ JS \p{P} (General_Category=Punctuation)
const NUMBER_QUERY = /^\d{1,7}$/

// TextNormalize.kt와 동일: 소문자 + 공백/문장부호 제거
export function normalizeQuery(s) {
  return s.toLowerCase().replace(STRIP, '')
}

let catalog = null // { meta:{version,count}, songs:[[brand,number,title,artist],...], normTitle:[], normArtist:[] }
let aliasGroups = new Map() // 정규화 아티스트 -> 같은 그룹의 다른 표기들
let loadPromise = null

function parseAliasTsv(tsv) {
  const map = new Map()
  for (const line of tsv.split('\n')) {
    const names = [...new Set(line.trim().split('\t').map((s) => s.trim()).filter(Boolean))]
    if (names.length < 2) continue
    for (const name of names) {
      const others = map.get(name) || []
      for (const other of names) {
        if (other !== name && !others.includes(other)) others.push(other)
      }
      map.set(name, others)
    }
  }
  return map
}

function aliasesFor(q) {
  if (!q) return []
  const prefixAllowed = q.length >= 3
  const out = new Set()
  for (const [name, others] of aliasGroups) {
    if (name === q || (prefixAllowed && name.startsWith(q))) {
      for (const other of others) if (other !== q) out.add(other)
    }
  }
  return [...out]
}

export function loadCatalog() {
  if (!loadPromise) {
    loadPromise = (async () => {
      const [res, aliasRes] = await Promise.all([
        fetch('songs.json', { cache: 'default' }),
        // 별칭 파일이 없어도 기본 검색은 계속 동작해야 한다.
        fetch('artist_aliases.tsv', { cache: 'default' }).catch(() => null),
      ])
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
      aliasGroups = aliasRes && aliasRes.ok ? parseAliasTsv(await aliasRes.text()) : new Map()
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

// CatalogQuery.kt와 동일한 결과/순서:
// - 곡명/가수명 부분 일치
// - 숫자만 입력하면 곡번호 정확/접두 일치를 우선
// - TJ/금영 아티스트 표기 차이는 별칭 그룹으로 확장
// ORDER BY 번호정확 > 번호접두 > 제목접두 > 가수(별칭)접두 > 제목 > 브랜드, LIMIT 300
export function search(rawQuery, brand, limit = 300) {
  if (!catalog) return []
  const q = normalizeQuery(rawQuery)
  if (!q) return []
  const aliases = aliasesFor(q)
  const numeric = NUMBER_QUERY.test(q)
  const out = []
  const { songs, normTitle, normArtist } = catalog
  for (let i = 0; i < songs.length; i++) {
    const [b, number, title, artist] = songs[i]
    if (brand && b !== brand) continue
    const titleHit = normTitle[i].includes(q)
    const artistHit = normArtist[i].includes(q)
    const aliasHit =
      !artistHit &&
      aliases.some((a) => normArtist[i] === a || normArtist[i].startsWith(a))
    const numberText = String(number)
    const numberHit = numeric && numberText.startsWith(q)
    if (!titleHit && !artistHit && !aliasHit && !numberHit) continue
    out.push({
      brand: b,
      number,
      title,
      artist,
      titlePrefix: normTitle[i].startsWith(q) ? 1 : 0,
      artistPrefix: normArtist[i].startsWith(q) || aliasHit ? 1 : 0,
      numberExact: numeric && numberText === q ? 1 : 0,
      numberPrefix: numberHit ? 1 : 0,
    })
  }
  out.sort((a, b) => {
    if (a.numberExact !== b.numberExact) return b.numberExact - a.numberExact
    if (a.numberPrefix !== b.numberPrefix) return b.numberPrefix - a.numberPrefix
    if (a.titlePrefix !== b.titlePrefix) return b.titlePrefix - a.titlePrefix
    if (a.artistPrefix !== b.artistPrefix) return b.artistPrefix - a.artistPrefix
    const t = a.title.localeCompare(b.title, 'ko')
    if (t !== 0) return t
    return a.brand < b.brand ? -1 : a.brand > b.brand ? 1 : 0
  })
  return out.slice(0, limit)
}
