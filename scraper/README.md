# norebang scraper

TJ미디어·금영 곡번호 카탈로그 수집기. Node 18+ / sqlite3 CLI만 필요 (npm 의존성 없음).

## 부트스트랩 (1회)

```bash
cd scraper
node tj.mjs            # TJ 전체 카탈로그 (요청 1회) -> data/tj.json
node ky.mjs            # KY 커버셋 크롤 (수 시간, 중단 시 재실행하면 이어서) -> data/ky.json
node build-db.mjs      # -> dist/songs.db, dist/songs.db.gz, dist/version.json
```

## 월간 갱신

```bash
cd scraper
node tj.mjs            # TJ는 매번 전체 재수집 (1요청) — publishdate(등록일) 포함
node ky-latest.mjs     # KY는 이달의 신곡만 증분 (/latest/ = 이달분만 제공)
node ky-backfill.mjs   # (선택) 건너뛴 달 보정 — 최근 TJ 등록곡 중 KY에 없는 제목만 검색
node build-db.mjs
```

`ky-latest.mjs`는 **이번 달 신곡만** 받으므로 한 달 이상 건너뛰면 그 사이 곡이 영구히
빠진다. 그럴 때 `ky-backfill.mjs`를 돌린다. 기준일은 `app/src/main/assets/songs.db`의
`meta.version`(또는 `node ky-backfill.mjs 2026-06-11`처럼 직접 지정)이고, TJ 등록일이
그 날짜 이후인 곡 중 KY에 제목이 없는 것만 곡제목 검색으로 찾아 병합한다.
진행 상황은 `data/ky-backfill-progress.json`에 체크포인트되므로 중단 후 재실행하면
이어서 진행한다 (대략 분당 40건).

> 검색어는 **원본 제목 그대로** 써야 한다. kysing.kr은 라틴 문자 검색이 대소문자를
> 구분하므로 정규화(소문자·공백 제거)한 제목으로 질의하면 결과가 0건이 된다.

산출물 배포: `dist/songs.db.gz` + `dist/version.json`을 공개 repo `kirin765/norebang-data`(main)에 push.
앱은 `version.json`의 `version`(YYYY-MM-DD)이 로컬보다 새로우면 `db_url`에서 내려받아 교체한다.

앱 번들 갱신: `dist/songs.db`를 `app/src/main/assets/songs.db`로 복사하면 신규 설치도 최신 시작.
