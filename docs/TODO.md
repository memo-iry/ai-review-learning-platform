# 남은 작업

3일차 발표 기준. 백엔드는 마무리됐고 프론트 연결이 남았습니다.

## 프론트 — 버그

### 1. Dashboard 링크가 로그인 화면으로 튕깁니다

라우터에서 대시보드가 `path: ''` 라 실제 URL 은 `/home` 인데,
`HomePage.vue` 의 네비게이션은 `/home/dashboard` 를 가리킵니다.
매칭이 안 되면 catch-all 에 걸려 `/` 로 리다이렉트됩니다.

```
라우터              네비게이션 링크
/home               /home/dashboard   ← 매칭 없음
/home/analysis      /home/analysis    OK
/home/review        /home/review      OK
/home/growth        /home/growth      OK
```

둘 중 하나로 맞추면 됩니다.

```js
// A. router/index.js — 경로를 명시한다 (권장)
{ path: 'dashboard', name: 'dashboard', ... }

// B. HomePage.vue — 링크를 고친다
<RouterLink to="/home">Dashboard</RouterLink>
```

### 2. AppLayout 이 화면에서 빠졌습니다

`App.vue` 에서 `<AppLayout>` 래퍼가 사라졌는데 `import` 와 `currentStep` 은 남아 있습니다.
상단 진행 단계 표시가 안 나옵니다. 의도한 것이면 안 쓰는 코드를 지우고,
실수면 다시 감싸면 됩니다.

## 프론트 — 로그인 연결

백엔드 API 는 준비됐습니다. Swagger 에서 눌러볼 수 있습니다.

```
POST /api/auth/login    { email, password } → { userId, email, name, role }
GET  /api/auth/me       세션 확인. 새로고침 후 로그인 복원용
POST /api/auth/logout

데모 계정
  learner@skala.com / demo   교육생
  admin@skala.com   / demo   운영자
```

- `fetch` 에 `credentials: 'include'` 가 필요합니다. 없으면 세션 쿠키가 안 실립니다
- `sessionStorage` 는 필요 없습니다. `/api/auth/me` 로 복원됩니다
- `ReflectionPage.vue` 의 `userId: 2` 하드코딩을 로그인 사용자로 바꿔야 합니다
- `role` 로 화면을 나눕니다
  - `LEARNER` 대시보드 · 회고 · 복습
  - `ADMIN` `GET /api/admin/overview` 개념별 평균 이해도

로그인이 붙으면 `application.yml` 의 `app.security.enforce` 를 `true` 로 바꿉니다.
그때부터 로그인 없는 요청은 401, 남의 데이터 요청은 403 입니다.
지금은 `false` 라 로그인 없이도 기존 화면이 그대로 동작합니다.

## 프론트 — 여유 되면

지난 복습자료를 다시 볼 화면이 없습니다. API 는 있습니다.

```
GET /api/users/{userId}/reviews    목록
GET /api/reviews/{reviewId}        상세 (핵심 개념 · 예제 · 확인 문제 · 응시 기록)
```

기획서 7절 "이전 학습자료 조회", 20절 Phase 2 "학습 추적" 에 해당합니다.

## 백엔드 — 완료

```
POST /api/auth/login · GET /api/auth/me · POST /api/auth/logout
GET  /api/lectures · /api/lectures/{id} · /api/lectures/{id}/materials
POST /api/reflections · /api/reflections/{id}/analyze
POST /api/quizzes/{id}/attempts
GET  /api/users/{id}/mastery · /api/users/{id}/reviews · /api/reviews/{id}
GET  /api/admin/overview
```

- 9개 테이블 · 마이그레이션 7개
- AI 확장 지점을 `AiAnalysisPort` 인터페이스로 분리, Mock 어댑터는 프로파일로 교체
- 회고 → 분석 → 복습 → Quiz → 이해도 갱신 사이클 완결
- 세션 인증과 역할별 접근 통제

## 결정 대기

- **컨테이너화 범위** — `backend/Dockerfile` 이 추가됐지만 compose 는 없습니다.
  미니 프로젝트 범위는 Local Demo 라 배포는 필수가 아닙니다.
  발표 5번 "향후 확장" 에 한 줄로 넣을지 정해야 합니다
- **`app.security.enforce` 전환 시점** — 프론트 로그인이 붙은 뒤
- **발표자료** — 15분 + Q&A 5분

## 시연 직전

`supabase/demo-reset.sql` 을 Supabase SQL Editor 에서 실행하면 데모 시작 상태로 돌아갑니다.

```
Level 2 이해 · 진도 50% · 평균 이해도 47%
Spring AI 와 AI 웹 서비스 설계 강의에는 회고 없음 — 그 자리에서 작성해 시연
```
