# AI 후행 학습 플랫폼

강의자료와 학습자의 회고를 함께 분석해 개인별 맞춤 복습자료를 제공하는 서비스입니다.

기존 PDF 요약 서비스는 같은 강의를 들은 모든 사람에게 같은 결과를 줍니다. 하지만
학습자마다 이해한 부분과 부족한 부분은 다릅니다. 이 서비스는 문서가 아니라 **학습자를
중심으로** 분석합니다.

> 같은 강의, 다른 복습.

## 핵심 흐름

```
강의 선택 → 강의자료 확인 → 학습 회고 작성
   → AI 이해도 분석 → 맞춤 복습자료 → Quiz → 이해도 갱신
```

## AI-Ready 설계

이번 단계에서는 실제 LLM 을 붙이지 않습니다. **AI 가 들어올 자리를 인터페이스로 비워 두고**
Mock 구현으로 전체 흐름을 검증합니다.

```
Frontend → Backend API → AiAnalysisPort → MockAiAnalysisAdapter   현재
Frontend → Backend API → AiAnalysisPort → OpenAiAnalysisAdapter   이후
```

교체 지점은 한 곳입니다.

```
backend/src/main/java/com/skala/ailearning/ai/AiAnalysisPort.java          인터페이스
backend/src/main/java/com/skala/ailearning/ai/MockAiAnalysisAdapter.java   @Profile("!openai")
```

새 구현체에 `@Profile("openai")` 를 붙이면 컨트롤러·서비스·프론트엔드를 고치지 않고 전환됩니다.

| 확장 지점 | 입력 | 저장 위치 |
|---|---|---|
| Reflection Analyzer | 강의자료 + 회고 | `ai_analyses` |
| Review Generator | 이해도 + 강의자료 | `personalized_reviews` |
| Quiz Generator | 취약 개념 | `quizzes` |

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Backend | Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Gradle |
| Frontend | Vue 3 · Vite 5 |
| Database | Supabase PostgreSQL 17.6 |
| 스키마 관리 | Supabase CLI 마이그레이션 |

## 실행

### 1. DB 접속 정보

`backend/.env` 를 만들고 비밀번호만 채웁니다.

```bash
cd backend
cp .env.example .env
```

```bash
DB_URL=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.whvadgfunkpmllizweom
DB_PASSWORD=          # 팀에 공유된 값
```

접속은 **Session pooler(5432)** 만 사용합니다.

- Direct connection 은 IPv6 전용이라 국내 망에서 연결되지 않습니다
- Transaction pooler(6543) 는 Hibernate 의 prepared statement 와 충돌합니다

### 2. Backend

```bash
cd backend
./gradlew bootRun
```

`backend/.env` 는 `spring.config.import` 로 자동 로드됩니다. IntelliJ 실행 구성에
환경 변수를 따로 넣을 필요가 없습니다. `backend` 폴더를 Gradle 프로젝트로 열고
`BackendApplication` 을 실행하면 됩니다.

`.env` 가 없으면 기동이 이렇게 실패합니다. 로컬 DB 에 조용히 붙는 것보다 낫습니다.

```
IllegalArgumentException: 'url' must start with "jdbc"
```

```
http://localhost:8080
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

```
http://localhost:5173
```

## 폴더 구조

```
ai-review-learning-platform/
├── backend/
│   └── src/main/java/com/skala/ailearning/
│       ├── ai/          AI 확장 지점 · Mock 어댑터 · 분석 응답
│       ├── common/      예외 처리 · CORS
│       ├── lecture/     강의 · 강의자료
│       ├── mastery/     개념별 학습 수준
│       ├── quiz/        Quiz · 응시 기록
│       ├── reflection/  회고 작성 및 분석
│       └── user/        사용자
├── frontend/src/
│   ├── api/             Backend API 호출
│   ├── components/      공통 레이아웃
│   └── pages/           대시보드 · 회고 · 분석 · 복습 · 성장
├── docs/erd.dbml        dbdiagram.io 용 ERD
└── supabase/migrations/ 스키마 및 시드 SQL
```

## API

Backend 실행 후 Swagger UI 에서 전체 명세를 보고 직접 요청할 수 있습니다.

```
http://localhost:8080/swagger-ui.html      Swagger UI
http://localhost:8080/v3/api-docs          OpenAPI JSON
```

| Method | Endpoint | 응답 | 설명 |
|---|---|---|---|
| POST | `/api/auth/login` | 200 · 401 | 로그인 (세션 생성) |
| GET | `/api/auth/me` | 200 · 401 | 현재 로그인 사용자 |
| POST | `/api/auth/logout` | 204 | 로그아웃 |
| GET | `/api/lectures` | 200 | 강의 목록 (최신순) |
| GET | `/api/lectures/{lectureId}` | 200 · 404 | 강의 상세 |
| GET | `/api/lectures/{lectureId}/materials` | 200 · 404 | 강의별 자료 |
| POST | `/api/reflections` | 201 · 400 · 403 · 404 | 회고 저장 |
| POST | `/api/reflections/{reflectionId}/analyze` | 200 · 403 · 404 | 이해도 분석 및 복습자료 생성 |
| POST | `/api/quizzes/{quizId}/attempts` | 201 · 403 · 404 | Quiz 응시 및 채점 |
| GET | `/api/users/{userId}/mastery` | 200 · 403 · 404 | 개념별 이해도 |
| GET | `/api/users/{userId}/reviews` | 200 · 403 · 404 | 복습자료 목록 |
| GET | `/api/reviews/{reviewId}` | 200 · 403 · 404 | 복습자료 상세 |
| GET | `/api/admin/overview` | 200 · 401 · 403 | 교육생 이해도 집계 (운영자 전용) |

오류 응답은 형식이 하나입니다.

```json
{ "message": "회고를 찾을 수 없습니다: 999" }
```

회고 저장 요청:

```json
{
  "userId": 2,
  "lectureId": 3,
  "understood": "RAG 기본 구조와 Embedding 역할은 이해했다",
  "difficult": "Chunking 기준이랑 Vector Store 검색 방식이 아직 헷갈린다",
  "wantsToLearn": "Spring AI에서 실제 RAG 구현 과정을 다시 보고 싶다"
}
```

분석 응답:

```json
{
  "understandingScore": 57,
  "understoodTopics": ["RAG", "Embedding"],
  "weakTopics": ["Chunking", "Vector Store"],
  "reviewMaterial": {
    "title": "Spring AI 맞춤 복습",
    "coreConcepts": ["..."],
    "exampleCode": "...",
    "quiz": [{ "question": "...", "answer": "..." }]
  }
}
```

같은 회고를 다시 제출하거나 다시 분석해도 새 행이 쌓이지 않고 갱신됩니다. 시연 도중 두 번
눌러도 제약 위반이 나지 않습니다.

## 인증과 접근 통제

로그인하면 서버가 세션을 만들고 쿠키로 사용자를 식별합니다. `userId` 를 파라미터로
받아 신뢰하지 않습니다 — 받는 순간 그것은 인증이 아니라 요청값이라 위조됩니다.

```
LEARNER   자기 데이터만 조회·수정 가능. 남의 userId 요청은 403
ADMIN     교육생 데이터 조회 가능 + /api/admin/** 접근 가능
```

브라우저에서 호출할 때 `credentials: 'include'` 가 필요합니다.

```js
fetch(url, { credentials: 'include', ... })
```

### 통제 스위치

```yaml
app:
  security:
    enforce: ${SECURITY_ENFORCE:false}
```

프론트에 로그인 화면이 붙기 전까지 `false` 로 둡니다. 이 상태에서는 로그인하지 않아도
기존 흐름이 그대로 동작합니다. 로그인이 붙으면 `true` 로 바꿉니다.

```
false   로그인 없이 호출 가능. /api/admin/** 은 그래도 막힘
true    로그인 없으면 401, 남의 데이터면 403
```

**운영자 전용 경로는 스위치와 무관하게 항상 막힙니다.** 스위치는 프론트를 기다리는
임시 조치이고, 프론트는 운영자 API 를 호출하지 않으므로 완화할 이유가 없습니다.

### 범위 밖

세션은 애플리케이션 메모리에 있습니다. 서버가 두 대가 되면 로그인이 유지되지 않으므로
운영에서는 Redis 세션 저장소나 JWT 가 필요합니다. Spring Security 도입도 함께 검토합니다.

## 데이터 모델

9개 테이블입니다. 전체 정의는 `docs/erd.dbml` 과 `supabase/migrations/` 에 있습니다.

```
users ─┬─ reflections ── ai_analyses ── personalized_reviews ── quizzes
       │       │                                                   │
       │   lectures ── lecture_materials                     quiz_attempts
       │                                                           │
       └─ concept_mastery ─────────────────────────────────────────┘
```

- `reflections` 는 `users ↔ lectures` 를 잇는 N:M 연결 테이블입니다 (한 강의당 회고 하나)
- `quiz_attempts` 는 `users ↔ quizzes` 를 잇습니다 (재응시 허용)
- `concept_mastery.level` 은 `score` 에서 파생됩니다 — 두 척도를 따로 관리하지 않습니다

```
score  0-25 → Level 1 인지    26-50 → Level 2 이해
      51-80 → Level 3 적용   81-100 → Level 4 구현
```

## 데모 데이터

| 구분 | 값 |
|---|---|
| 학습자 | `learner@skala.com` · `user_id = 2` |
| 운영자 | `admin@skala.com` |
| 강의 | 컨테이너 · Vue.js · Spring AI · AI 웹 서비스 설계 |

프론트엔드는 현재 `userId = 2` 를 사용합니다. 인증은 MVP 범위 밖입니다.

시연 전 상태를 되돌리려면 `supabase/demo-reset.sql` 을 Supabase SQL Editor 에
붙여넣고 실행합니다. 회고·분석·복습자료·Quiz·응시기록을 지우고 아래 상태로 맞춥니다.

```
학습 수준   Level 2 이해
학습 진도   50%  (회고 2 / 강의 4)
평균 이해도  47%  (Vue 55 · Docker 58 · Kubernetes 42 · Vue Router 35)
```

Spring AI 와 AI 웹 서비스 설계 강의에는 회고가 없습니다. 시연에서 그 자리에 회고를
작성해 분석 → 복습 → Quiz → 이해도 변화까지 실시간으로 보여주기 위한 구성입니다.

`password` 는 구조 확인용 예시 값입니다. 실제 로그인을 붙일 때 Spring Security 와 BCrypt 가
필요합니다.

## 스키마 변경

Supabase 대시보드에서 테이블을 직접 만들지 않습니다. 반드시 마이그레이션으로 관리합니다.

```bash
supabase migration new <이름>     # supabase/migrations/ 에 파일 생성
supabase db push                  # 원격 반영
supabase migration list           # 로컬·원격 동기화 확인
```

마이그레이션 파일은 Git 에 커밋합니다. DB 비밀번호와 접속 문자열은 올리지 않습니다.

## MVP 범위 밖

실제 LLM 연결 · PDF 파싱 · Chunking · Embedding · Vector DB · RAG 질의응답 ·
Quiz 결과 기반 이해도 자동 갱신 · 장기 학습 데이터 분석 · 스터디 매칭 · 강사 대시보드
