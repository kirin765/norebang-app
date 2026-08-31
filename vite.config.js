import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// 노래방 번호검색 — Apps in Toss 미니앱 웹 번들
export default defineConfig({
  plugins: [
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.png', 'apple-touch-icon-180x180.png'],
      manifest: {
        name: '노래방 번호검색',
        short_name: '노래방 번호검색',
        description: '곡명·가수명으로 노래방(TJ/금영) 곡 번호를 바로 찾는 검색 앱',
        lang: 'ko',
        theme_color: '#5B2EFF',
        background_color: '#FCFBFF',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        icons: [
          { src: 'pwa-64x64.png', sizes: '64x64', type: 'image/png' },
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
          { src: 'maskable-icon-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // 곡 DB(11MB)는 precache에서 제외하고 첫 fetch 시 CacheFirst로 캐시 —
        // SW 설치가 큰 파일 때문에 지연/실패하지 않도록
        maximumFileSizeToCacheInBytes: 2 * 1024 * 1024,
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /songs\.json$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'norebang-catalog',
              expiration: { maxEntries: 1, maxAgeSeconds: 30 * 24 * 60 * 60 },
            },
          },
        ],
      },
    }),
  ],
  build: {
    // sql.js wasm + 곡 DB는 public/에서 그대로 복사 (hashed 하지 않음)
    assetsInlineLimit: 0,
  },
})