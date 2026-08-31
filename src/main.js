// 노래방 번호검색 — 웹 미니앱 (MainActivity.kt의 Compose UI를 그대로 이식, 한국어 UI)
import { loadCatalog, search, songCount, dbVersion } from './catalog.js'
import './style.css'

const BRAND_COLORS = { TJ: '#2E7DFF', KY: '#E5484D' }
const BRAND_LABELS = { TJ: 'TJ', KY: '금영' }
const FAV_STORAGE_KEY = 'norebang:favorites'

const state = {
  query: '',
  brand: null, // null = 전체, 'TJ', 'KY'
  results: [],
  favorites: loadFavorites(),
  tab: 0, // 0=검색 1=즐겨찾기
}

function loadFavorites() {
  try {
    return JSON.parse(localStorage.getItem(FAV_STORAGE_KEY)) || []
  } catch {
    return []
  }
}

function saveFavorites() {
  localStorage.setItem(FAV_STORAGE_KEY, JSON.stringify(state.favorites))
}

function favKey(song) {
  return `${song.brand}:${song.number}`
}

function isFav(song) {
  return state.favorites.some((f) => favKey(f) === favKey(song))
}

function toggleFavorite(song) {
  const k = favKey(song)
  if (isFav(song)) {
    state.favorites = state.favorites.filter((f) => favKey(f) !== k)
  } else {
    state.favorites = [{ ...song, addedAt: Date.now() }, ...state.favorites]
  }
  saveFavorites()
  render()
}

// ---- 렌더링 ----
const app = document.getElementById('app')

function render() {
  const q = state.query.trim()
  const tab = state.tab

  app.innerHTML = `
    <div class="screen">
      <main class="scroll">
        ${tab === 0 ? renderSearch() : renderFavorites()}
      </main>
      <nav class="bottom-nav">
        <button class="nav-item ${tab === 0 ? 'active' : ''}" data-tab="0">
          <span class="nav-icon">🔍</span><span>검색</span>
        </button>
        <button class="nav-item ${tab === 1 ? 'active' : ''}" data-tab="1">
          <span class="nav-icon">♡</span><span>즐겨찾기</span>
        </button>
      </nav>
    </div>
  `

  app.querySelectorAll('.nav-item').forEach((btn) => {
    btn.addEventListener('click', () => {
      state.tab = Number(btn.dataset.tab)
      render()
    })
  })

  if (tab === 0) {
    const input = app.querySelector('#search-input')
    input.addEventListener('input', () => {
      state.query = input.value
      scheduleSearch()
    })
    app.querySelectorAll('.brand-chip').forEach((chip) => {
      chip.addEventListener('click', () => {
        state.brand = chip.dataset.brand === 'ALL' ? null : chip.dataset.brand
        runSearch()
        render()
      })
    })
  }

  app.querySelectorAll('.fav-btn').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation()
      const song = JSON.parse(btn.dataset.song)
      toggleFavorite(song)
    })
  })
}

function renderSearch() {
  return `
    <div class="search-area">
      <input
        id="search-input"
        class="search-input"
        type="search"
        placeholder="곡명 또는 가수명 검색"
        value="${escapeHtml(state.query)}"
        autocomplete="off"
        aria-label="곡명 또는 가수명 검색"
      />
      <div class="chip-row">
        ${[
          ['ALL', '전체'],
          ['TJ', 'TJ'],
          ['KY', '금영'],
        ]
          .map(
            ([key, label]) =>
              `<button class="brand-chip ${state.brand === (key === 'ALL' ? null : key) ? 'active' : ''}" data-brand="${key}">${label}</button>`
          )
          .join('')}
      </div>
    </div>
    ${renderResultBody()}
  `
}

function renderResultBody() {
  if (state.query.trim() === '') {
    return `
      <div class="empty">
        <div class="empty-emoji">🎤</div>
        <div class="empty-text">부르고 싶은 곡을 검색하세요</div>
        <div class="db-info">곡 DB ${escapeHtml(dbVersion())} · ${Number(songCount()).toLocaleString()}곡</div>
      </div>
    `
  }
  if (state.results.length === 0) {
    return `<div class="empty"><div class="empty-emoji">🔍</div><div class="empty-text">검색 결과가 없습니다</div></div>`
  }
  return `
    <ul class="song-list">
      ${state.results.map(renderSongRow).join('')}
    </ul>
  `
}

function renderFavorites() {
  if (state.favorites.length === 0) {
    return `
      <div class="empty">
        <div class="empty-emoji">♡</div>
        <div class="empty-text">검색 결과에서 하트를 눌러<br/>자주 부르는 곡을 저장하세요</div>
      </div>
    `
  }
  return `
    <ul class="song-list">
      ${state.favorites.map(renderSongRow).join('')}
    </ul>
  `
}

function renderSongRow(song) {
  const color = BRAND_COLORS[song.brand] || '#666'
  const label = `${BRAND_LABELS[song.brand] || song.brand} ${song.number}`
  const fav = isFav(song)
  return `
    <li class="song-row">
      <span class="brand-badge" style="color:${color};background:${color}1f">${escapeHtml(label)}</span>
      <div class="song-text">
        <div class="song-title">${escapeHtml(song.title)}</div>
        <div class="song-artist">${escapeHtml(song.artist)}</div>
      </div>
      <button class="fav-btn ${fav ? 'on' : ''}" data-song='${escapeAttr(JSON.stringify(song))}' aria-label="${fav ? '즐겨찾기 해제' : '즐겨찾기 추가'}">
        ${fav ? '❤️' : '🤍'}
      </button>
    </li>
  `
}

// ---- 검색 (120ms 디바운스 — 네이티브와 동일) ----
let debounceTimer = null
function scheduleSearch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(runSearch, 120)
}

function runSearch() {
  state.results = search(state.query, state.brand)
  render()
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))
}

function escapeAttr(s) {
  return escapeHtml(s)
}

// ---- 초기화 ----
;(async () => {
  await loadCatalog()
  render()
})()