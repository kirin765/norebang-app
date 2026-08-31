import { defineConfig, type AppsInTossConfig } from '@apps-in-toss/web-framework/config';

// Apps in Toss (앱인토스) SDK 3 configuration.
// - appName: unique app key — deep link intoss://norebang-number + origin
//   https://norebang-number.web.tossmini.com. Immutable once registered in the
//   Apps in Toss console; must match console registration exactly.
// - Display name / icon / screenshots are managed in the console, not here.
const config: AppsInTossConfig = {
  appName: 'norebang-number',
  brand: {
    primaryColor: '#5B2EFF', // 노래방 번호검색 브랜드 보라 — 네이티브 앱과 동일
  },
  permissions: [], // 검색 + 즐겨찾기(localStorage)만 사용 — 민감 권한 없음
  webView: {
    bounces: false,
    pullToRefreshEnabled: false,
    overScrollMode: 'never',
    allowsBackForwardNavigationGestures: false,
    allowsInlineMediaPlayback: true,
  },
  webBundleDir: 'dist', // vite build 출력 — index.html 포함
};

export default defineConfig(config);