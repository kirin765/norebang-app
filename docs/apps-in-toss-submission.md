# Apps in Toss 등록 준비 — 노래방 번호검색 (`norebang-number`)

> Apps in Toss(앱인토스) **게시 플랫폼** 등록 문서. 앱 UI는 한국어 유지(기존
> 네이티브 앱 디자인 · 브랜드 보라 `#5B2EFF` 그대로) — 앱인토스용으로 UI를
> 리스타일하지 않는다.

## 1. 빌드 산출물

| 항목 | 값 |
|---|---|
| `.ait` 파일 | `norebang-number.ait` (SDK 3, `@apps-in-toss/web-framework@3.0.5`) |
| 웹 번들 | `dist/` (Vite build) — `index.html`, `assets/*`, `songs.json`, PWA(`sw.js`, `manifest.webmanifest`) |
| 검증 | `unzip -p norebang-number.ait bundle.json` → `config.appName == "norebang-number"` ✅ |

빌드 명령: `npm run build:ait` (= `npm run db:build && vite build && ait build`)

- `db:build`: `app/src/main/assets/songs.db`(130,599곡) → `public/songs.json`
- `*.ait`는 `.gitignore`에 추가됨

## 2. 콘솔 등록 폼 데이터

| 필드 | 값 | 규칙 |
|---|---|---|
| 한국어 앱 이름 | **노래방 번호검색** (7자) | ≤10자 · 이후 수정 가능 |
| 영어 앱 이름 | **Noraebang** (9자) | ≤15자 · 발음 쉬운 명사 · 명령형/동사구 금지 |
| appName | **`norebang-number`** | 고유 ID · **등록 후 불변** · `intoss://norebang-number` 딥링크 · 콘솔과 config 정확히 일치 |
| 부제 | **곡명·가수명으로 노래방 번호 즉시 검색** (19자) | ≤20자 · 구체적 사용 가치 · 비속어/느낌표 금지 |
| 상세 설명 | 아래 참조 | 사용자가 보는 것 → 누르는 것 → 경험하는 것 순서 |
| 앱 유형 | **비게임 (비게임)** | 게임 아님 — GRAC 등급분류 불필요 |
| 사용 연령 | **전체이용가** | 콘솔 이용등급과 일치 (GRAC 비대상) |
| 웹보드 게임 여부 | **아니오** | 보드게임 아님 — 스토어 링크 제약 없음 |

### 상세 설명 (초안)

> 노래방에서 부르고 싶은 곡, 번호가 기억나지 않으셨나요?
>
> **노래방 번호검색**은 TJ · 금영 노래방의 곡 번호를 13만 곡(2026-06-11 기준
> 130,599곡) 카탈로그에서 즉시 찾아주는 검색 앱입니다.
>
> **이런 분께 좋아요**
> - 금영(TJ·KY) 번호가 헷갈릴 때 — 검색창에 곡명 또는 가수명을 입력
> - "이 노래 TJ 몇 번이지?" — 노래방 기기 앞에서 바로 확인
> - 자주 부르는 곡을 저장해 두고 싶을 때 — 하트를 눌러 즐겨찾기에 보관
>
> **사용 방법**
>
> 1. 곡명 또는 가수명을 검색창에 입력하세요
> 2. 전체 / TJ / 금영 필터로 원하는 반주기기만 골라볼 수 있어요
> 3. 결과에서 하트(❤️)를 누르면 다음에 바로 찾아볼 수 있도록 저장됩니다
>
> 즐겨찾기는 기기에 자동 저장되어 언제든 다시 열람할 수 있습니다. 곡 데이터는
> 정기적으로 갱신됩니다. (게임머니·베팅·결제 없음)

## 3. 콘솔 자산 (promo/ait-assets/)

| 자산 | 파일 | 규격 |
|---|---|---|
| 앱 로고 | `app-icon-600.png` | 600×600 PNG · 정사각형 · **모서리 없음** · 불투명 배경 |
| 다크 모드 로고 | `app-icon-dark-600.png` | 600×600 PNG · 동일 규칙 · 어두운 배경 |
| 썸네일 | `thumbnail-1932x828.png` | 1932×828 |
| 스크린샷(세로) | `screenshot-1-home-636x1048.png` `screenshot-2-search-636x1048.png` `screenshot-3-favorites-636x1048.png` | 636×1048 PNG × 3 (이상) |
| 스크린샷(가로) | `screenshot-4-search-1504x741.png` | 1504×741 PNG × 1 (이상) |
| 검색 키워드 | 아래 참조 | 한국어 + 영어 |

재생성: `npm run build` 후 `npm run preview` 상태에서 `npm run capture:assets`
(Playwright). 스크린샷은 실제 웹 앱 UI를 캡처한 것.

### 검색 키워드 (한국어 + 영어)

- 한국어: 노래방, 노래방 번호, 노래방 번호검색, TJ, 금영, KY, 금영 번호, TJ 번호, 노래방 검색, 노래 번호, 반주, 곡 검색, 가수 검색
- 영어: karaoke, karaoke number, karaoke number search, norebang, TJ, KY, kumyoung, karaoke song number, norebang number

## 4. GRAC (게임물등급) — 해당 없음

본 앱은 **비게임** 앱(노래방 곡 번호 검색 유틸리티)으로 게임물관리위원회
(GRAC) 등급분류 대상이 아니다. 이용등급 `전체이용가`는 콘솔의 일반 이용등급
항목으로 처리한다. 자세한 근거는 `docs/grac/README.md` 참조.

## 5. 테스트 & 출시 순서

1. `npm run build:ait` → `norebang-number.ait` 생성
2. 앱인토스 콘솔에 업로드 (상기 등록 폼 데이터 + 자산)
3. 콘솔의 등급/심사 단계 진행 (비게임 — GRAC 서류 불필요)
4. **샌드박스 테스트**: Toss 앱에서 `intoss://norebang-number` 진입
5. **릴리즈 QR 테스트**: `intoss-private://norebang-number` (Toss 앱 내 QR 스캔)
6. 전체 플로우(검색 → 필터 → 즐겨찾기 저장/재조회) 확인 후 **출시**

### 백엔드/CORS

이 앱은 백엔드 호출이 없고(곡 DB는 번들 `songs.json`), 즐겨찾기는 브라우저
`localStorage`에 저장된다. 별도 CORS 설정 불필요. 향후 서버 연동 시에는
다음을 허용해야 한다:

- `https://norebang-number.web.tossmini.com`
- `https://norebang-number.private-web.tossmini.com`

## 6. 참고: SDK 3 릴리스는 비가역

앱인토스는 SDK 3 릴리스 후 SDK 2로 롤백할 수 없다. 등록 전 콘솔 자동 검증
(앱 이름/부제/자산 규격)과 샌드박스 테스트를 반드시 완료하라.