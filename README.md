# AI 후행 학습 플랫폼

강의자료와 학습자의 회고를 함께 분석해 개인별 맞춤 복습자료를 제공하는 서비스입니다.

> 같은 강의, 다른 복습.

SKALA AI 활용 웹 서비스 개발 미니 프로젝트 · Memo:iry

| 문서 | 내용 |
|---|---|
| [`docs/rag-design.md`](docs/rag-design.md) | RAG 파이프라인과 System Prompt 설계 |
| [`docs/erd.dbml`](docs/erd.dbml) | ERD (dbdiagram.io) |
| [`docs/api.http`](docs/api.http) | 엔드포인트 호출 예시 |

---

## 1. 서비스 기획

### 문제

강의는 다 같이 끝나지만 막힌 지점은 저마다 다릅니다.

- 복습을 하려면 **무엇을 모르는지 먼저 정리**해야 합니다. 그 정리가 부담이라 시작이 미뤄집니다
- 시간이 지나면 어디가 막혔는지조차 기억나지 않아, 처음부터 다시 배우는 비용이 듭니다
- 강사는 수업이 끝난 뒤 **누가 어디서 막혔는지** 확인할 방법이 없습니다

기존 PDF 요약 서비스는 같은 강의를 들은 모두에게 같은 결과를 줍니다.
이 서비스는 문서가 아니라 **학습자를 중심으로** 분석합니다.

### 해결

교육생은 이미 회고를 씁니다. 다만 파일로 남고 끝납니다.
그 회고를 **분석의 입력**으로 쓰면, 무엇을 모르는지 정리하는 일을 시스템이 대신합니다.

```
강의 선택 → 강의자료 확인 → 학습 회고 작성
   → 이해도 분석 → 맞춤 복습자료 → Quiz → 개념별 이해도 갱신
                                              ↑                 │
                                              └─────────────────┘
                              누적된 이해도가 다음 회고의 분석 기준이 된다
```

순환이 닫혀 있는 것이 핵심입니다. Quiz 채점 결과가 개념별 이해도로 돌아가고,
다음 회고는 그 이해도 위에서 분석됩니다.

### Use-Case

| Actor | 하는 일 |
|---|---|
| 교육생 `LEARNER` | 강의 선택 · 강의자료 확인 · 회고 작성 · 이해도 분석 확인 · 복습자료 열람 · Quiz 응시 · 지난 회고 조회 |
| 운영자 `ADMIN` | 개념별 평균 이해도 확인 · 공통으로 막힌 주제 파악 · 다음 수업 보완 지점 결정 |

역할은 로그인할 때 서버가 판정합니다. 화면에서 고르는 값이 아닙니다.
운영자 집계는 개인 식별 정보 없이 집계값만 반환합니다.

### 범용 챗봇과의 차이

| | 범용 챗봇 | 이 서비스 |
|---|---|---|
| 학습 이력 | 지난주에 무엇을 틀렸는지 모름 | 회고와 Quiz 결과가 개념별로 누적 |
| 강의자료 | 갖고 있지 않음 | 강의·강의자료가 분석 입력에 포함 |
| 시작점 | 무엇을 물어야 할지 알아야 함 | 막힌 지점을 먼저 짚어 줌 |

모델 성능이 아니라 **누적된 학습 맥락**이 차이를 만듭니다.

---

## 2. AI-Ready 설계

이번 단계에서는 실제 LLM 을 붙이지 않습니다.
**AI 가 들어올 자리를 인터페이스로 비워 두고** 규칙 기반 구현으로 전체 흐름을 검증했습니다.

```
Frontend → Backend API → AiAnalysisPort → MockAiAnalysisAdapter    현재 (규칙 기반)
Frontend → Backend API → AiAnalysisPort → OpenAiAnalysisAdapter    이후 (LLM)
```

교체 지점은 한 곳입니다.

```java
public interface AiAnalysisPort {
    AiAnalysisResult analyze(AnalysisCommand command);
}
```

새 구현체에 `@Profile("openai")` 와 `@Primary` 를 붙이면
컨트롤러 · 서비스 · DB · 프론트엔드를 고치지 않고 전환됩니다.

| 확장 지점 | 입력 | 저장 위치 |
|---|---|---|
| Reflection Analyzer | 강의자료 + 회고 | `ai_analyses` · `concept_mastery.summary` |
| Review Generator | 취약 개념 + 강의자료 | `personalized_reviews` |
| Quiz Generator | 취약 개념 | `quizzes` |

프롬프트는 역할별로 셋으로 나누되 **인터페이스는 하나로 둡니다.**
어댑터 안에서 세 번 호출하고 하나의 결과로 조립합니다. 포트를 쪼개면
서비스가 호출 순서를 알아야 하고, 교체 지점이 한 곳이라는 성질을 잃습니다.

검색(RAG)도 어댑터 안에서 일어납니다. `AnalysisCommand` 에 검색 결과를 필드로 더하지
않으므로 계약이 바뀌지 않습니다. 자세한 내용은 [`docs/rag-design.md`](docs/rag-design.md).

### 규칙 기반 구현에서 얻은 제약

규칙 기반으로 먼저 만들면서 **무엇이 틀릴 수 있는지**를 겪었고,
그 목록이 그대로 시스템 프롬프트의 제약 조건이 됐습니다.

- 회고에 없는 사실을 만들어내지 않는다 — 판단 근거는 회고 문장 안에 있어야 한다
- 같은 개념이 이해와 취약에 모두 나오면 취약을 우선한다
- 개념 이름이 다른 단어 안에서 걸리지 않게 한다 (`Vue` 가 `RouterView` 안에서 매칭되던 문제)

---

## 3. 시스템 아키텍처

### 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Backend | Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Gradle |
| Frontend | Vue 3 · Vite 5 · axios |
| Database | Supabase PostgreSQL 17.6 |
| 스키마 관리 | Supabase CLI 마이그레이션 |
| API 문서 | springdoc-openapi (Swagger UI) |

### 데이터 모델

9개 테이블입니다. 전체 정의는 [`docs/erd.dbml`](docs/erd.dbml) 과 `supabase/migrations/` 에 있습니다.

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
- `concept_mastery.summary` 는 분석기가 만든 자연어 상태 서술입니다. 이후 복습자료 생성
  프롬프트에 실립니다. 파생 값이며 갱신 시 덮어쓰므로, 이력은 `ai_analyses` 와
  `reflections` 에 남습니다. Quiz 채점은 점수만 바꾸고 서술은 건드리지 않습니다

```
score  0-25 → Level 1 인지    26-50 → Level 2 이해
      51-80 → Level 3 적용   81-100 → Level 4 구현
```

회고 분석과 Quiz 채점, **두 경로 모두** 개념별 이해도를 갱신합니다.

```
회고 분석    이해한 개념 +15 · 취약 개념 −20  (이번 회고의 이해도 점수 기준)
Quiz 채점    정답 +12 · 오답 −8               (개념별로 따로 반영)
```

### API

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
| POST | `/api/reflections` | 201 · 400 · 403 · 404 | 회고 저장 (강의당 1건, 다시 쓰면 갱신) |
| GET | `/api/reflections?userId=` | 200 · 403 · 404 | 내 회고 목록 |
| POST | `/api/reflections/{reflectionId}/analyze` | 200 · 403 · 404 | 이해도 분석 및 복습자료 생성 |
| GET | `/api/quizzes?userId=` | 200 · 403 · 404 | 내 Quiz 목록 |
| GET | `/api/quizzes/{quizId}` | 200 · 403 · 404 | Quiz 단건 (정답·해설 제외) |
| GET | `/api/quizzes/attempts?userId=` | 200 · 403 · 404 | Quiz 응시 이력 |
| POST | `/api/quizzes/{quizId}/attempts` | 201 · 403 · 404 | Quiz 응시 및 채점 |
| GET | `/api/users/{userId}/mastery` | 200 · 403 · 404 | 개념별 이해도 (점수 · 서술) |
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
  "levelBefore": 2,
  "levelAfter": 2,
  "reviewMaterial": {
    "reviewId": 1,
    "quizId": 1,
    "title": "Spring AI 맞춤 복습",
    "coreConcepts": ["..."],
    "exampleCode": "...",
    "quiz": [{ "conceptName": "Chunking", "question": "...", "options": ["..."] }]
  }
}
```

Quiz 조회 응답에는 **정답과 해설이 없습니다.** 채점 응답에만 내려갑니다 —
클라이언트가 정답을 들고 있으면 서버 채점은 형식만 남습니다.

같은 회고를 다시 제출하거나 다시 분석해도 새 행이 쌓이지 않고 갱신됩니다.
시연 도중 두 번 눌러도 제약 위반이 나지 않습니다.

### 인증과 접근 통제

로그인하면 서버가 세션을 만들고 쿠키로 사용자를 식별합니다. `userId` 를 파라미터로
받아 신뢰하지 않습니다 — 받는 순간 그것은 인증이 아니라 요청값이라 위조됩니다.

```
로그인하지 않은 요청은 401
LEARNER   자기 데이터만 조회·수정 가능. 남의 userId 요청은 403
ADMIN     교육생 데이터 조회 가능 + /api/admin/** 접근 가능
```

`/api/auth/**` 와 `/api/lectures/**` 는 공개입니다. 나머지는 세션이 필요합니다.
비밀번호는 BCrypt 해시로 저장하며, 없는 계정과 비밀번호 불일치를 **같은 401** 로 답합니다.
구분해서 알려주면 계정 존재 여부가 드러납니다.

브라우저에서 호출할 때 자격 증명을 함께 보내야 합니다.

```js
axios.create({ withCredentials: true })
```

---

## 4. 실행과 데모

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

무료 티어 pooler 는 클라이언트 15개가 한도입니다. 팀원이 나눠 쓰므로
인스턴스당 커넥션을 2개로 제한하고 유휴 연결을 30초 만에 반납합니다.

### 2. Backend

```bash
cd backend
./gradlew bootRun          # http://localhost:8080
```

`backend/.env` 는 `spring.config.import` 로 자동 로드됩니다. IntelliJ 실행 구성에
환경 변수를 따로 넣을 필요가 없습니다.

`.env` 가 없으면 기동이 이렇게 실패합니다. 로컬 DB 에 조용히 붙는 것보다 낫습니다.

```
IllegalArgumentException: 'url' must start with "jdbc"
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev                # http://localhost:5173
```

CORS 허용 origin 이 `http://localhost:5173` 이므로 다른 포트로 뜨면 로그인이 막힙니다.
Vite 가 포트를 밀어 올렸다면 기존 프로세스를 정리하고 다시 띄웁니다.

### 데모 데이터

| 구분 | 값 |
|---|---|
| 교육생 | `learner@skala.com` / `demo` |
| 운영자 | `admin@skala.com` / `demo` |
| 강의 | 컨테이너 · Vue.js · Spring AI · AI 웹 서비스 설계 |

시연 전 상태를 되돌리려면 `supabase/demo-reset.sql` 을 실행합니다.
회고 · 분석 · 복습자료 · Quiz · 응시기록을 지우고 아래 상태로 맞춥니다.

```
학습 수준   Level 2 이해
학습 진도   50%  (회고 2 / 강의 4)
평균 이해도  47%  (Docker 58 · Vue 55 · Kubernetes 42 · Vue Router 35)
```

**Vue.js 와 컨테이너 강의에는 baseline 회고가 있습니다.** 그 강의에 회고를 쓰면
`UNIQUE(user_id, lecture_id)` 때문에 덮어써집니다.
테스트는 **Spring AI** 나 **AI 웹 서비스 설계** 강의로 하십시오.

### 시연 시나리오

```
1  로그인          learner@skala.com / demo
2  대시보드        Level 2 · 진도 50% · 복습이 필요한 개념 3개
3  Spring AI 선택   강의자료 확인
4  회고 작성        "RAG는 이해했다 / Chunking과 Vector Store가 헷갈린다"
5  이해도 분석      이해 RAG·Embedding / 취약 Chunking·Vector Store
6  Quiz 응시        하나는 일부러 틀린다
7  채점 결과        점수 · 오답 해설 · 개념별 이해도 변화
8  대시보드 복귀     진도 50% → 75%
```

4번을 그 자리에서 입력해도 결과가 따라 바뀝니다. 미리 만들어 둔 화면이 아닙니다.

---

## 5. 회고와 향후 확장

### 겪은 문제

**커넥션 풀이 한도를 넘었습니다.** 무료 티어 Session Pooler 는 클라이언트 15개가 한도인데
각자 5개씩 열자 세 명만으로 소진되어 나머지는 기동조차 실패했습니다.
인스턴스당 2개로 줄이고 유휴 반납을 30초로 잡았습니다.
인스턴스 수와 DB 한도를 함께 설계해야 한다는 걸 배웠습니다.

**JSONB 는 안의 형식을 검증해 주지 않습니다.** 확인 문제를 서술형에서 4지선다로 바꿨는데
컬럼 타입이 그대로라 스키마 검증을 통과했습니다. 예전 형식 행이 남아 응시하면 500 이 났습니다.
컬럼 타입이 같아도 안의 형식이 바뀌면 데이터 마이그레이션이 필요합니다.

**개념 이름이 다른 단어 안에서 걸렸습니다.** `Vue` 가 `RouterView` 안에 들어 있어
라우팅 회고가 엉뚱하게 분류됐습니다. 토큰 경계를 보도록 고쳤고,
한글은 조사가 붙으므로 이 규칙에서 제외했습니다.

### 잘한 판단

**Mock 을 어댑터 한 곳에만 뒀습니다.** 처음에는 화면에도 복습자료를 넣어 두었는데,
그러면 같은 내용이 두 곳에 생기고 뒤를 실제 AI 로 바꿔도 화면은 바뀌지 않습니다.
걷어내고 인터페이스 뒤 한 곳으로 모았습니다.

**순환을 끝까지 닫았습니다.** 복습자료까지만 만들고 멈추면 "확인 문제는 왜 있나"에
답할 수 없습니다. 채점 결과가 개념별 이해도로 돌아가게 이으니 서비스의 주장이 성립했습니다.

### 현재 한계

분석기는 **개념 사전 11개에 기반한 규칙**입니다. 사전에 없는 주제는 잡지 못합니다.
강의자료도 제목까지만 입력에 들어가고 본문은 아직 읽지 않습니다.
이 두 가지가 실제 LLM 을 붙였을 때 가장 크게 달라질 부분입니다.

세션은 애플리케이션 메모리에 있습니다. 서버가 두 대가 되면 로그인이 유지되지 않으므로
운영에서는 Redis 세션 저장소나 JWT 가 필요합니다.

### MVP 범위 밖

```
실제 LLM 연결 · PDF 파싱 · Chunking · Embedding · Vector DB · RAG 질의응답
장기 학습 데이터 분석 · 스터디 매칭 · 강사용 화면 · 배포 파이프라인
```

### 다음 단계

| 단계 | 내용 |
|---|---|
| 1 · 실제 LLM | `AiAnalysisPort` 구현체 교체 · PDF 파싱 · Chunking · Embedding · RAG 복습자료 생성 |
| 2 · 학습 추적 | 개념별 이해도 추이 그래프 · 복습 주기 추천 · 운영자 화면 |
| 3 · 운영 준비 | Redis 세션 또는 JWT · 컨테이너 이미지 · 인스턴스별 커넥션 풀 설계 |

`feat/llm-adapter` 브랜치에 1단계 착수분이 있습니다.
프롬프트 3종과 `OpenAiAnalysisAdapter` 를 구현했으나 **실제 API 호출 검증은 남아 있습니다.**

---

## 부록

### 폴더 구조

```
ai-review-learning-platform/
├── backend/
│   └── src/main/java/com/skala/ailearning/
│       ├── ai/          AI 확장 지점 · 규칙 기반 어댑터 · 분석 응답
│       ├── common/      예외 처리 · CORS · 세션 접근 통제
│       ├── lecture/     강의 · 강의자료
│       ├── mastery/     개념별 학습 수준 · 운영자 집계
│       ├── quiz/        Quiz · 응시 기록 · 채점
│       ├── reflection/  회고 작성 및 분석
│       └── user/        사용자 · 인증
├── frontend/src/
│   ├── api/             Backend API 호출 (axios · 세션 쿠키)
│   ├── components/      공통 레이아웃 · 디자인 시스템
│   ├── stores/          로그인 상태 · 학습 진행 상태
│   └── pages/           랜딩 · 로그인 · 대시보드 · 강의목록 · 회고 · 분석 · 회고기록 · Quiz
├── docs/
│   ├── erd.dbml         dbdiagram.io 용 ERD
│   ├── api.http         엔드포인트 호출 예시
│   └── rag-design.md    RAG · System Prompt 설계
└── supabase/
    ├── migrations/      스키마 및 시드 SQL
    └── demo-reset.sql   시연 직전 초기화
```

### 스키마 변경

Supabase 대시보드에서 테이블을 직접 만들지 않습니다. 반드시 마이그레이션으로 관리합니다.

```bash
supabase migration new <이름>     # supabase/migrations/ 에 파일 생성
supabase db push                  # 원격 반영
supabase migration list           # 로컬·원격 동기화 확인
```

`ddl-auto: validate` 이므로 애플리케이션이 테이블을 만들지 않습니다.
엔티티와 실제 스키마가 어긋나면 기동 단계에서 실패합니다.

마이그레이션 파일은 Git 에 커밋합니다. DB 비밀번호와 접속 문자열은 올리지 않습니다.
