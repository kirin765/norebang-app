// TJ ↔ 금영 아티스트 표기 통합 데이터 생성.
//
// 문제: TJ는 아이유를 "IU", 금영은 "아이유(IU)"로 표기한다. 그래서 "아이유"로
// 검색하면 금영 곡만 나오고 TJ 곡번호는 누락된다. 반대로 "IU"로 검색하면 둘 다
// 나온다. 두 브랜드가 같은 곡(정규화 제목 동일)을 같은 아티스트로 부르는 경우를
// 집계해, 표기가 다른 아티스트를 하나의 그룹으로 묶는다.
//
// 신뢰도 조건(오탐 방지):
//   - 같은 정규화 제목이 양쪽 브랜드에 있어야 함
//   - 두 아티스트 표기 조합이 2곡 이상에서 반복
//   - 한쪽은 한글, 다른 쪽은 비한글 (스크립트가 다를 때만 별칭 필요)
//   - 각 아티스트의 교차 브랜드 곡 중 50% 이상이 상대와 짝을 이룸
//
// 출력은 탭 구분 TSV 한 줄 = 한 그룹. 앱(코틀린)과 웹(JS)이 같은 파일을 쓴다.

const HANGUL = /[가-힣]/
const MIN_PAIR_SONGS = 2
const MIN_DOMINANCE = 0.5

function hasHangul(s) {
  return HANGUL.test(s)
}

/** @param {Array<{brand:string, norm_title:string, norm_artist:string}>} rows */
export function buildArtistAliasGroups(rows) {
  /** @type {Map<string, {TJ:Set<string>, KY:Set<string>}>} */
  const byTitle = new Map()
  for (const r of rows) {
    if (!r.norm_title || !r.norm_artist) continue
    let entry = byTitle.get(r.norm_title)
    if (!entry) {
      entry = { TJ: new Set(), KY: new Set() }
      byTitle.set(r.norm_title, entry)
    }
    if (r.brand === 'TJ') entry.TJ.add(r.norm_artist)
    else if (r.brand === 'KY') entry.KY.add(r.norm_artist)
  }

  const pairCount = new Map()
  const tjTotal = new Map()
  const kyTotal = new Map()
  const bump = (map, key) => map.set(key, (map.get(key) || 0) + 1)

  for (const { TJ, KY } of byTitle.values()) {
    if (TJ.size === 0 || KY.size === 0) continue
    for (const t of TJ) bump(tjTotal, t)
    for (const k of KY) bump(kyTotal, k)
    for (const t of TJ) {
      for (const k of KY) {
        const key = `${t}\t${k}`
        pairCount.set(key, (pairCount.get(key) || 0) + 1)
      }
    }
  }

  /** @type {Array<[string, string]>} */
  const edges = []
  for (const [key, count] of pairCount) {
    if (count < MIN_PAIR_SONGS) continue
    const [tj, ky] = key.split('\t')
    if (hasHangul(tj) === hasHangul(ky)) continue
    if (count / tjTotal.get(tj) < MIN_DOMINANCE) continue
    if (count / kyTotal.get(ky) < MIN_DOMINANCE) continue
    edges.push([tj, ky])
  }

  // union-find: A~B, B~C -> 하나의 그룹
  const parent = new Map()
  const find = (x) => {
    if (!parent.has(x)) parent.set(x, x)
    let root = x
    while (parent.get(root) !== root) root = parent.get(root)
    while (parent.get(x) !== root) {
      const next = parent.get(x)
      parent.set(x, root)
      x = next
    }
    return root
  }
  const union = (a, b) => {
    const ra = find(a)
    const rb = find(b)
    if (ra !== rb) parent.set(ra, rb)
  }
  for (const [a, b] of edges) union(a, b)

  const groups = new Map()
  for (const key of parent.keys()) {
    const root = find(key)
    if (!groups.has(root)) groups.set(root, new Set())
    groups.get(root).add(key)
  }

  return [...groups.values()]
    .map((set) => [...set].sort())
    .filter((group) => group.length >= 2)
    .sort((a, b) => a[0].localeCompare(b[0]))
}

export function renderAliasTsv(groups) {
  return groups.map((group) => group.join('\t')).join('\n') + (groups.length ? '\n' : '')
}
