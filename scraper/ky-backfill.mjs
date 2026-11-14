// KY backfill: fills gaps when the monthly incremental (ky-latest.mjs) was skipped
// for one or more months. /latest/ only covers the current month, so songs released
// in the skipped months never get picked up.
//
// Strategy: take the TJ titles registered on/after a cutoff date that have no KY title
// match, run a KY title search for each, and merge the rows whose title matches the TJ
// title. Unlike ky.mjs this is not a syllable coverset crawl, so it costs one request
// per missing release instead of a multi-hour full recrawl.
//
// Usage:
//   node ky-backfill.mjs                 # cutoff = version date in app/src/main/assets/songs.db
//   node ky-backfill.mjs 2026-06-11      # explicit cutoff
//
// Data: reads data/tj.json + data/ky.json, writes data/ky.json (merge, never drops).
// Progress is checkpointed to data/ky-backfill-progress.json, so re-running resumes.
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { DatabaseSync } from 'node:sqlite';
import { fetchRetry, sleep, normalize, parseKyPage } from './lib.mjs';

const CONC = 4;      // parallel title searches
const DELAY_MS = 200;
const MAX_PAGES = 3; // title searches rarely need more
const PROGRESS = 'data/ky-backfill-progress.json';
const DB = '../app/src/main/assets/songs.db';

function bundledVersion() {
  if (!existsSync(DB)) return null;
  const db = new DatabaseSync(DB, { readOnly: true });
  const row = db.prepare("SELECT value FROM meta WHERE key='version'").get();
  db.close();
  return row ? String(row.value) : null;
}

const cutoff = process.argv[2] || bundledVersion();
if (!cutoff) throw new Error('no cutoff date: pass YYYY-MM-DD or build songs.db first');
console.log(`backfill cutoff: TJ songs registered on/after ${cutoff}`);

const tj = JSON.parse(readFileSync('data/tj.json', 'utf8'));
const ky = JSON.parse(readFileSync('data/ky.json', 'utf8'));
const byNo = new Map(ky.map((s) => [s.no, s]));
const kyTitles = new Set(ky.map((s) => normalize(s.title)));

const isHangul = (ch) => ch >= '가' && ch <= '힣';
const hasText = (t) => [...t].some((ch) => isHangul(ch) || (ch >= 'a' && ch <= 'z'));
const relevant = (t) => t.length >= 2 && hasText(t);

// normalized title match: identical, or one contains the other (subtitle variants)
function titleMatches(target, candidate) {
  if (!candidate || !relevant(candidate)) return false;
  if (candidate === target) return true;
  const [short, long] = target.length <= candidate.length ? [target, candidate] : [candidate, target];
  return short.length >= 2 && long.includes(short);
}

const progress = existsSync(PROGRESS)
  ? JSON.parse(readFileSync(PROGRESS, 'utf8'))
  : { cutoff, done: [] };
const done = new Set(progress.done);

// Search with the raw title: kysing.kr is case-sensitive for latin keywords and
// expects the phrase as written ("EPIKISM" matches, "epikism" returns nothing).
// The normalized form is only used to compare results against TJ titles.
const recent = tj.filter((s) => !s.date || s.date >= cutoff);
const queries = new Map(); // raw title -> normalized target
for (const s of recent) {
  const raw = s.title.trim();
  const target = normalize(raw);
  if (!relevant(target)) continue;
  if (kyTitles.has(target) || queries.has(raw)) continue;
  queries.set(raw, target);
}
const todo = [...queries.keys()].filter((raw) => !done.has(raw));
console.log(`backfill: ${recent.length} recent TJ songs, ${todo.length} titles to search (${done.size} already done, ${byNo.size} KY songs known)`);

let added = 0, failed = 0, processed = 0;
const started = Date.now();

async function searchPages(keyword) {
  const rows = [];
  let page = 1, maxPage = 1;
  while (page <= maxPage && page <= MAX_PAGES) {
    const batch = [];
    for (let i = 0; i < CONC && page <= maxPage && page <= MAX_PAGES; i++, page++) {
      const p = page;
      batch.push(fetchRetry(
        `https://kysing.kr/search/?category=2&keyword=${encodeURIComponent(keyword)}&s_page=${p}`,
      ).then(async (res) => parseKyPage(await res.text())));
    }
    const results = await Promise.all(batch);
    for (const r of results) {
      rows.push(...r.rows);
      maxPage = Math.max(maxPage, r.maxPage);
    }
    await sleep(DELAY_MS);
  }
  return rows;
}

function save() {
  writeFileSync(PROGRESS, JSON.stringify({ cutoff, done: [...done] }));
  writeFileSync('data/ky.json', JSON.stringify([...byNo.values()]));
}

for (let i = 0; i < todo.length; i += CONC) {
  const chunk = todo.slice(i, i + CONC);
  const settled = await Promise.allSettled(chunk.map((t) => searchPages(t)));
  settled.forEach((res, idx) => {
    const keyword = chunk[idx];
    const target = queries.get(keyword);
    done.add(keyword);
    if (res.status === 'rejected') {
      failed++;
      return;
    }
    for (const s of res.value) {
      if (byNo.has(s.no)) continue;
      if (!titleMatches(target, normalize(s.title))) continue;
      byNo.set(s.no, s);
      kyTitles.add(normalize(s.title));
      added++;
    }
  });
  processed += chunk.length;
  await sleep(DELAY_MS);
  if (processed % 40 < CONC) {
    save();
    const perMin = processed / ((Date.now() - started) / 60000);
    const eta = perMin > 0 ? Math.round((todo.length - processed) / perMin) : -1;
    console.log(`  ${processed}/${todo.length} searched — +${added} new (total ${byNo.size}), ${perMin.toFixed(1)}/min, ETA ~${eta}min`);
  }
}
save();
console.log(`KY backfill: +${added} songs, total ${byNo.size}${failed ? ` (${failed} queries failed)` : ''}`);
