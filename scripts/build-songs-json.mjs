// songs.db → public/songs.json (웹용 곡 카탈로그)
// node:sqlite는 Node 22.5+ 내장 — 별도 의존성 없음.
// 결과 형식: { meta: {version, count}, songs: [[brand, number, title, artist], ...] }
// - norm_* 컬럼은 제외 (JS 정규화와 130,599행 전수 비교 일치 확인, 크기 ~35% 절감)
// - raw JSON 단일 파일 (gz 아님): 호스팅 오리진이 .gz에 Content-Encoding:gzip을 붙이면
//   브라우저가 자동 해제하여 이중 압축해제 문제가 생기므로 raw로 고정한다.
import { DatabaseSync } from 'node:sqlite'
import { writeFileSync, mkdirSync, statSync } from 'node:fs'

const SRC_DB = process.env.SONGS_DB || 'app/src/main/assets/songs.db'
const OUT_DIR = 'public'
const OUT_JSON = `${OUT_DIR}/songs.json`

const db = new DatabaseSync(SRC_DB, { readOnly: true })

const metaRow = db.prepare("SELECT value FROM meta WHERE key='version'").get()
const version = metaRow ? String(metaRow.value) : '?'
const count = db.prepare('SELECT COUNT(*) AS c FROM songs').get().c

const rows = db
  .prepare('SELECT brand, number, title, artist FROM songs')
  .all()

const payload = { meta: { version, count }, songs: rows.map((r) => [r.brand, r.number, r.title, r.artist]) }
const json = JSON.stringify(payload)

mkdirSync(OUT_DIR, { recursive: true })
writeFileSync(OUT_JSON, json)

console.log(`catalog written: version=${version} count=${count}`)
console.log(`raw  : ${(json.length / 1024 / 1024).toFixed(2)} MB -> public/songs.json`)

const srcSize = statSync(SRC_DB).size
console.log(`source songs.db: ${(srcSize / 1024 / 1024).toFixed(2)} MB`)