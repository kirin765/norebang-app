# GRAC(게임물관리위원회) 등급분류 — 노래방 번호검색은 **비게임**

## 결론 (2026-08-23 확인)

**노래방 번호검색은 비게임(비게임) 앱이다.** 노래방 곡 번호 검색 유틸리티로
게임물관리위원회(GRAC)의 게임물 등급분류 대상이 **아니다.**

- 앱인토스 콘솔에서 앱 유형을 **비게임**으로 선택한다.
- GRAC 등급분류번호 · 등급분류증명서 PDF는 **필요 없다.**
- 사용 연령 **전체이용가**는 콘솔의 일반 이용등급 항목으로 입력한다
  (게임 등급분류 단계 자체가 비게임 앱에는 표시되지 않음).
- 게임머니·베팅·결제 요소가 없으므로 게임 관련 심사 항목 해당 없음.

> ⚠️ 사전예방: 앱을 **게임**(예: 리듬 게임, 노래 맞히기 게임)으로 분류하거나
> 콘솔에서 '게임' 유형으로 등록하면 GRAC 등급분류가 **필수**가 된다.
> 그 경우 아래 `query-grac.py`로 등급분류번호를 먼저 조회하고, 0건이면
> GRAC 심의(약 10~15일) → 등급분류증명서 PDF 발급 → 콘솔 첨부 순서를 따른다.

## GRAC 조회 스크립트 (게임으로 전환 시 대비용)

`query-grac.py` — 게임물명으로 등급분류번호 조회 (자체등급분류 조회 +
등급분류결정 확인, 두 사이트 동시 검색).

```bash
python3 docs/grac/query-grac.py "노래방"          # 0건 예상 (비게임)
python3 docs/grac/query-grac.py "리듬게임명"       # 게임 전환 시 재조회
```

- 자체등급분류: <https://www.grac.or.kr/Statistics/SelfRateGameStatistics.aspx>
- 등급분류결정: <https://www.grac.or.kr/Statistics/GameStatistics.aspx>
- 등급분류번호 포맷: `GC-CC-NP-YYMMDD-###`, `SC-OM-YYMMDD-###`, `CC-NP-YYMMDD-###`
- 0건 = GRAC 미등록 → 심의 신청 안내 (`grac-rating-application.md` 참조)

## 2026-08-23 조회 결과 (노래방 번호검색)

- `자체등급분류 게임물 조회`: **0건** ("노래방 번호검색" 기준)
- `등급분류결정 확인`: **0건**

→ GRAC DB에 등록된 게임물이 없다. **비게임 앱이라 등급분류 대상이 아니라는
  판정과 일치**한다. (`python3 docs/grac/query-grac.py "노래방 번호검색"`으로
  재조회 가능)

## 이용등급 콘솔 입력 (비게임)

`앱 정보 → 접근 연령(이용등급)`에서 **전체이용가** 선택. 콘솔의 '게임
등급분류' 단계는 게임 유형에서만 등장한다.

## 관련 문서

- `console-rating-checklist.md` — 게임 유형 전환 시 필요한 콘솔 입력 체크리스트
- `grac-rating-application.md` — 게임 유형 전환 시 GRAC 심의 신청 절차
- `../apps-in-toss-submission.md` — 전체 등록 폼 데이터/자산
- `../apps-in-toss-caution-compliance.md` — 주의사항/컴플라이언스