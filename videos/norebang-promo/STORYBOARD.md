---
format: 1080x1920
message: "노래방 곡 번호, 이제 헤매지 말고 3초 만에 찾자"
arc: Hook → Solution/Demo → Proof → CTA
audience: 노래방 자주 가는 사람 (Shorts/Reels 시청자)
mode: autonomous
music: none
language: ko
---

## Frame 1 — Hook: "번호 뭐였지?"

- scene: 노래방 페인 훅 — 큰 한글 타이포가 비트에 슬램
- duration: 2.8s
- poster: 2.2s
- transition_in: cut
- status: animated
- blueprint: kinetic-type-beats
- motion: kinetic-beat-slam
- src: compositions/frames/01-hook.html

앞의 실제 노래방 장면에서 바로 이어지는 브릿지 훅. 배경은 밝은 캔버스(#FCFBFF) + faint 도트 그리드. 화면 중앙에 흔한 상황을 큰 타이포로 때린다.

Time-coded shot:
- Scene 1 (0.0–0.9s): 상단에 작은 label-pill "🎤 노래방에서" 가 톡 튀어 들어옴(spring pop, 살짝 −2° 기울임). 동시에 중앙 대사 1행 "이 곡..." 이 scale-slam으로 들어온다.
- Scene 2 (0.9–1.8s): 중앙 대사가 "이 곡 번호 뭐였지?" 로 완성 — "번호" 두 글자만 accent 컬러(#5B2EFF 퍼플)로 크게, 나머지는 ink(#1A1526). "번호"에 얇은 tilt-deco 밑줄/하이라이트 블록.
- Scene 3 (1.8–2.8s): 리모컨 앞에서 멈칫하는 느낌 — 대사 아래 작은 회색 서브 "리모컨 앞에서 또 멈칫…" 이 페이드인, 물음표가 살짝 떨리는(shake) 마이크로 모션. 홀드.

## Frame 2 — Solution/Demo: 3초 검색

- scene: 폰 목업 안 앱 — 검색바 타이핑 → 결과 곡 리스트 팝인
- duration: 4.2s
- poster: 3.4s
- transition_in: crossfade
- status: animated
- blueprint: device-surface-showcase
- motion: discrete-text-sequence, grid-card-assemble
- src: compositions/frames/02-demo.html

핵심 데모. 세로 폰 목업(4px 블랙 보더 + 8px 하드 섀도, 네오브루탈)이 중앙 hero로 등장. 폰 안은 실제 앱 UI 재현.

Time-coded shot:
- Scene 1 (0.0–0.8s): 폰이 아래에서 spring-pop으로 올라와 살짝 +1.5° 기울어 안착. 상단 헤더 "🎤 노래방 번호검색" + 검색바(라운드, placeholder "곡명 또는 가수명 검색") 보임. 필터칩 [전체][TJ][금영] 노출.
- Scene 2 (0.8–1.8s): 검색바에 커서 깜빡이며 "아이유" 가 한 글자씩 타이핑(discrete-text-sequence). 좌측 상단 label-pill "3초면 끝" 이 톡 튀어나옴.
- Scene 3 (1.8–3.2s): 결과 곡 행이 위→아래 순차 cascade(grid-card-assemble)로 3개 팝인:
    · [TJ 47590] 좋은 날 — 아이유 / 하트
    · [금영 43897] 밤편지 — 아이유 / 하트
    · [TJ 62145] 내 손을 잡아 — 아이유 / 하트
  브랜드 배지는 TJ=블루(#2E7DFF) 틴트, 금영=레드(#E5484D) 틴트. 각 행 하단 얇은 divider.
- Scene 4 (3.2–4.2s): 첫 행의 하트가 회색→레드로 press-pop(즐겨찾기 저장), 그 행이 살짝 하이라이트. 폰 옆에 작은 tilt 별버스트 "탭!" 이 톡. 홀드.

## Frame 3 — Proof: TJ·금영 13만곡 오프라인

- scene: 카운트업 스탯 13만곡 + 특징 칩 3개
- duration: 2.6s
- poster: 2.2s
- transition_in: crossfade
- status: animated
- blueprint: dataviz-countup
- motion: counting-dynamic-scale, kinetic-beat-slam
- src: compositions/frames/03-proof.html

증거 프레임. 밝은 캔버스에 큰 숫자가 카운트업하며 커진다.

Time-coded shot:
- Scene 1 (0.0–1.0s): 중앙 초대형 숫자가 0 → 130,000 으로 카운트업(counting-dynamic-scale, tabular-nums), 값이 커질수록 살짝 scale-up. 숫자 아래 "곡" 단위와 "TJ · 금영 전곡" 라벨.
- Scene 2 (1.0–1.9s): 숫자 아래로 특징 label-pill 3개가 좌→우 slam으로 순차 등장:
    · "📴 오프라인 검색"  · "⚡ 3초 결과"  · "❤️ 즐겨찾기"
  (각 칩 4px 보더 + 하드 섀도, 서로 다른 파스텔 ground, ±2° tilt 교차)
- Scene 3 (1.9–2.6s): 상단 label-pill "인터넷 없어도" 가 톡 튀고, 전체가 살짝 breathe 하며 홀드.

## Frame 4 — CTA: 지금 무료 다운로드

- scene: 앱 아이콘 락업 + 다운로드 CTA (close-frame)
- duration: 2.8s
- poster: 2.3s
- transition_in: crossfade
- status: animated
- blueprint: logo-assemble-lockup
- motion: kinetic-beat-slam, press-release-spring
- src: compositions/frames/04-cta.html

클로저. blockframe의 close-frame — 다크 그라운드(#1A1526) + 화이트 보더 + 퍼플 컬러 섀도. 유일한 어두운 프레임으로 임팩트.

Time-coded shot:
- Scene 1 (0.0–0.9s): 중앙에 앱 아이콘 타일(퍼플 #5B2EFF, 4px 화이트 보더, 12px 퍼플 오프셋 섀도) 안에 "🎤" 가 spring-pop. 그 아래 앱 이름 "노래방 번호검색" 이 화이트로 slam-in.
- Scene 2 (0.9–1.8s): 이름 아래 서브카피 "곡명만 치면, 번호 끝." 가 페이드업.
- Scene 3 (1.8–2.8s): 하단에 CTA 버튼 "지금 무료 다운로드" (퍼플 ground, 블랙 보더, 하드 섀도) 가 press-release-spring으로 톡 안착 + 그 아래 "▶ Google Play" 작은 라벨. 버튼이 한 번 살짝 pulse 후 홀드.

## Video direction

- Canvas 1080×1920 세로. 디자인은 blockframe(네오브루탈) — 4px 블랙 보더, 8px 하드 오프셋 섀도, 스퀘어 코너, ±2°~+1.5° tilt 데코, label-pill이 모든 eyebrow. Pretendard 800–900 대문자급 굵기 한글 디스플레이.
- 컬러: 퍼플 #5B2EFF(주 accent/CTA), TJ 블루 #2E7DFF, 금영 레드 #E5484D(앱 UI 배지·하트 전용), ink #1A1526, canvas #FCFBFF. F1–F3 밝은 캔버스, F4만 다크 close-frame으로 대비.
- 배경(ground)은 각 프레임 full-bleed clip 레이어로 깔 것(#root 배경 금지). faint 도트 그리드는 코너 장식.
- 페이싱: 짧고 percussive. 각 프레임 마지막 0.5–0.8s는 홀드(포스터 안정). 무음이므로 모션 리듬이 곧 비트 — 슬램/스프링/카운트업으로 에너지.
- 금지: "광고없음" 문구 절대 사용 금지(AdMob 예정). 실제로 없는 리뷰/별점/수상 문구 날조 금지.
- 텍스트는 한국어. 이모지는 UI 재현·강조용으로만 소량.
