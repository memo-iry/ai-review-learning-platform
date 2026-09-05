# Memo:iry | AI 후행 학습 플랫폼

강의자료와 학습자의 회고를 함께 분석해 개인별 맞춤 복습자료를 제공하는 AI 후행 학습 플랫폼입니다.

> **같은 강의, 다른 복습.**

SKALA AI 활용 웹 서비스 개발 미니 프로젝트

| 문서 | 내용 |
|---|---|
| [`docs/rag-design.md`](docs/rag-design.md) | RAG 파이프라인과 시스템 프롬프트 설계 |
| [`docs/erd.dbml`](docs/erd.dbml) | ERD |
| [`docs/api.http`](docs/api.http) | API 호출 예시 |

---

## 1. 서비스 기획

### 문제

같은 강의를 들어도 이해한 부분과 어려움을 느낀 지점은 학습자마다 다릅니다.

- 복습을 시작하려면 먼저 **무엇을 모르는지 정리**해야 하지만 이 과정 자체가 부담이 됩니다.
- 시간이 지나면 어느 부분에서 막혔는지 기억하기 어려워 처음부터 다시 학습하는 경우가 생깁니다.
- 교육 운영자는 수업이 끝난 뒤 **학습자들이 어떤 개념에서 어려움을 겪고 있는지** 한눈에 파악하기 어렵습니다.

기존 PDF 요약 서비스는 문서의 내용을 요약하는 데 초점을 맞춥니다.

Memo:iry는 문서 자체보다 **학습자의 현재 이해 상태**에 초점을 맞춰 복습 방향을 제시합니다.

### 해결

교육생이 작성한 학습 회고를 분석 입력으로 활용해 이해한 내용과 어려운 내용을 구조화합니다.

분석 결과를 바탕으로 맞춤 복습자료와 퀴즈를 제공하고, 퀴즈 결과를 다시 개념별 이해도에 반영합니다.

```text
강의 선택
    ↓
강의자료 확인
    ↓
학습 회고 작성
    ↓
이해도 분석
    ↓
맞춤 복습자료
    ↓
퀴즈
    ↓
개념별 이해도 갱신
    ↓
대시보드 반영
```

현재 구현에서는 회고 분석과 퀴즈 채점 결과가 `concept_mastery`에 누적되고 대시보드와 운영자 집계에 반영됩니다.

향후 실제 LLM과 RAG를 연결하면 누적된 이해도를 다음 학습 분석의 추가 맥락으로 활용할 수 있도록 확장할 계획입니다.

### Use Case

| Actor | 주요 기능 |
|---|---|
| 교육생 `LEARNER` | 강의 선택, 강의자료 확인, 회고 작성, 이해도 분석 확인, 복습자료 열람, 퀴즈 응시, 지난 회고 조회 |
| 운영자 `ADMIN` | 개념별 평균 이해도 확인, 공통 취약 주제 파악, 교육 보완 지점 확인 |

사용자 역할은 로그인 과정에서 서버가 확인하며 화면에서 임의로 선택하지 않습니다.

운영자 API는 개인 식별 정보 대신 집계된 학습 현황을 반환합니다.

### 범용 챗봇과의 차이

| 구분 | 범용 챗봇 | Memo:iry |
|---|---|---|
| 학습 이력 | 이전 학습 결과가 지속적으로 관리되지 않음 | 회고와 퀴즈 결과를 개념별로 누적 |
| 강의자료 | 사용자가 매번 필요한 맥락을 제공해야 함 | 강의와 강의자료를 분석 맥락으로 활용 |
| 복습 시작점 | 사용자가 무엇을 질문할지 먼저 판단해야 함 | 회고를 바탕으로 취약 지점을 먼저 제시 |
| 학습 결과 | 대화가 끝나면 학습 결과가 별도로 남지 않음 | 퀴즈 결과를 개념별 이해도에 반영 |

Memo:iry의 핵심은 모델 자체보다 **누적되는 학습 맥락과 반복되는 학습 피드백 구조**에 있습니다.

---

## 2. AI-Ready 설계

이번 MVP에서는 실제 LLM을 연결하기 전에 규칙 기반 분석기로 전체 학습 흐름을 먼저 검증했습니다.

향후 AI 모델을 교체하더라도 서비스 계층의 변경을 최소화할 수 있도록 AI 기능의 경계를 인터페이스로 정의했습니다.

```text
현재

Frontend
    ↓
Backend API
    ↓
AiAnalysisPort
    ↓
MockAiAnalysisAdapter
    ↓
규칙 기반 분석
```

```text
향후

Frontend
    ↓
Backend API
    ↓
AiAnalysisPort
    ↓
OpenAiAnalysisAdapter
    ↓
LLM / RAG
```

AI 구현체의 교체 지점은 `AiAnalysisPort`입니다.

```java
public interface AiAnalysisPort {
    AiAnalysisResult analyze(AnalysisCommand command);
}
```

향후 새로운 구현체에 `@Profile("openai")`와 `@Primary`를 적용하면 컨트롤러, 서비스, DB, 프론트엔드의 계약을 유지하면서 AI 구현체를 교체할 수 있습니다.

### AI 역할 분리

| 역할 | 주요 입력 | 저장 위치 |
|---|---|---|
| Reflection Analyzer | 강의 정보, 학습 회고 | `ai_analyses`, `concept_mastery.summary` |
| Review Generator | 취약 개념, 강의자료 | `personalized_reviews` |
| Quiz Generator | 취약 개념 | `quizzes` |

프롬프트는 역할별로 세 가지로 나누되 외부에서 사용하는 인터페이스는 하나로 유지하도록 설계했습니다.

향후 `OpenAiAnalysisAdapter` 내부에서 다음 과정을 수행합니다.

```text
Reflection Analyzer
        ↓
Review Generator
        ↓
Quiz Generator
        ↓
AiAnalysisResult
```

역할마다 별도의 포트를 만들 경우 서비스 계층이 AI 호출 순서를 직접 관리해야 합니다.

Memo:iry에서는 AI 처리 과정을 어댑터 내부에 두어 서비스 계층이 특정 AI 구현 방식에 의존하지 않도록 구성했습니다.

RAG 검색 역시 향후 어댑터 내부에서 수행하도록 설계했습니다.

`AnalysisCommand`에 검색 결과 자체를 추가하지 않아 서비스 계층과 AI 계층 사이의 계약을 유지합니다.

자세한 내용은 [`docs/rag-design.md`](docs/rag-design.md)를 참고하세요.

### 규칙 기반 구현에서 확인한 제약 조건

규칙 기반 분석기를 먼저 구현하면서 실제 오분류 사례를 확인했고, 이를 향후 시스템 프롬프트의 제약 조건으로 정리했습니다.

- 회고에 작성되지 않은 사실을 임의로 생성하지 않습니다.
- 분석 판단의 근거는 학습자의 회고 내용에 있어야 합니다.
- 같은 개념이 이해한 내용과 어려운 내용에 모두 등장하면 어려운 내용을 우선합니다.
- 개념명이 다른 단어의 일부로 잘못 인식되지 않도록 토큰 경계를 확인합니다.

예를 들어 `Vue`가 `RouterView`에 포함되어 있다는 이유만으로 `Vue` 개념으로 잘못 분류되는 문제가 발생했습니다.

영문 개념은 토큰 경계를 확인하도록 수정하고, 조사가 붙을 수 있는 한글 표현은 별도 기준으로 처리했습니다.

---

## 3. 시스템 아키텍처

### 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Spring Data JPA, Gradle |
| Frontend | Vue 3, Vite 5, Axios |
| Database | Supabase PostgreSQL 17.6 |
| Schema Management | Supabase CLI Migration |
| API Documentation | springdoc-openapi, Swagger UI |

### 전체 구조

```text
User
  ↓
Vue 3
  ↓
REST API / Session Cookie
  ↓
Spring Boot
  ├── Controller
  ├── Service
  ├── Repository / JPA
  ├── Session / Access Control
  └── AiAnalysisPort
          ↓
    AI Adapter
  ↓
JDBC
  ↓
Supabase PostgreSQL
```

Frontend는 Supabase에 직접 접근하지 않고 Spring Boot Backend를 통해 데이터를 조회하고 변경합니다.

---

## 4. 데이터 모델

MVP는 총 9개의 테이블로 구성했습니다.

전체 정의는 [`docs/erd.dbml`](docs/erd.dbml)과 `supabase/migrations/`에서 확인할 수 있습니다.

```text
users
 ├── reflections
 │      ↓
 │   ai_analyses
 │      ↓
 │   personalized_reviews
 │      ↓
 │    quizzes
 │      ↓
 │  quiz_attempts
 │
 └── concept_mastery

lectures
 ├── lecture_materials
 └── reflections
```

### 핵심 관계

- `users` 1:N `reflections`
- `lectures` 1:N `reflections`
- `lectures` 1:N `lecture_materials`
- `reflections` 1:1 `ai_analyses`
- `ai_analyses` 1:1 `personalized_reviews`
- `personalized_reviews` 1:1 `quizzes`
- `quizzes` 1:N `quiz_attempts`
- `users` 1:N `quiz_attempts`
- `users` 1:N `concept_mastery`

### 주요 제약 조건

`reflections`는 사용자와 강의 조합당 하나의 회고만 저장합니다.

```text
UNIQUE(user_id, lecture_id)
```

`concept_mastery`는 사용자와 개념 조합당 하나의 현재 학습 상태를 관리합니다.

```text
UNIQUE(user_id, concept_name)
```

### Concept Mastery

Memo:iry는 하나의 전체 학습 수준보다 **개념별 학습 수준**을 관리합니다.

```text
score 0 - 25
→ Level 1 인지

score 26 - 50
→ Level 2 이해

score 51 - 80
→ Level 3 적용

score 81 - 100
→ Level 4 구현
```

`concept_mastery.level`은 `score`를 기준으로 계산합니다.

`concept_mastery.summary`는 분석기가 생성한 개념별 학습 상태 설명입니다.

향후 실제 LLM과 RAG를 연결할 때 학습자의 현재 상태를 프롬프트 맥락으로 제공하기 위해 저장합니다.

현재 값은 갱신 시 최신 상태로 변경하며, 이전 분석 내용은 `ai_analyses`와 `reflections`에 남습니다.

### 이해도 갱신

회고 분석과 퀴즈 채점 모두 개념별 이해도에 반영됩니다.

```text
회고 분석

이해한 개념    +15
취약 개념      -20
```

```text
퀴즈 채점

정답           +12
오답            -8
```

각 점수는 개념별로 계산해 `concept_mastery`에 반영합니다.

---

## 5. API

Backend를 실행하면 Swagger UI에서 전체 API 명세를 확인하고 직접 호출할 수 있습니다.

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

### Authentication

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| POST | `/api/auth/login` | 200 / 401 | 로그인 및 세션 생성 |
| GET | `/api/auth/me` | 200 / 401 | 현재 로그인 사용자 조회 |
| POST | `/api/auth/logout` | 204 | 로그아웃 |

### Lecture

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| GET | `/api/lectures` | 200 | 강의 목록 조회 |
| GET | `/api/lectures/{lectureId}` | 200 / 404 | 강의 상세 조회 |
| GET | `/api/lectures/{lectureId}/materials` | 200 / 404 | 강의자료 조회 |

### Reflection

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| POST | `/api/reflections` | 201 / 400 / 403 / 404 | 회고 저장 |
| GET | `/api/reflections?userId=` | 200 / 403 / 404 | 내 회고 목록 조회 |
| POST | `/api/reflections/{reflectionId}/analyze` | 200 / 403 / 404 | 이해도 분석 및 복습자료 생성 |

동일한 사용자가 같은 강의에 회고를 다시 작성하면 새로운 데이터를 추가하지 않고 기존 회고를 갱신합니다.

### Review

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| GET | `/api/users/{userId}/reviews` | 200 / 403 / 404 | 복습자료 목록 조회 |
| GET | `/api/reviews/{reviewId}` | 200 / 403 / 404 | 복습자료 상세 조회 |

### Quiz

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| GET | `/api/quizzes?userId=` | 200 / 403 / 404 | 내 퀴즈 목록 조회 |
| GET | `/api/quizzes/{quizId}` | 200 / 403 / 404 | 퀴즈 단건 조회 |
| GET | `/api/quizzes/attempts?userId=` | 200 / 403 / 404 | 퀴즈 응시 이력 조회 |
| POST | `/api/quizzes/{quizId}/attempts` | 201 / 403 / 404 | 퀴즈 응시 및 채점 |

퀴즈 조회 응답에는 정답과 해설을 포함하지 않습니다.

채점이 완료된 뒤 응답에서만 정답과 해설을 제공하며, 실제 채점은 서버에서 수행합니다.

### Mastery

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| GET | `/api/users/{userId}/mastery` | 200 / 403 / 404 | 개념별 이해도 조회 |

### Admin

| Method | Endpoint | 응답 코드 | 설명 |
|---|---|---|---|
| GET | `/api/admin/overview` | 200 / 401 / 403 | 교육생 이해도 집계 조회 |

운영자 API에서는 사용자별 순위를 제공하지 않고 전체 학습 상태를 집계해 제공합니다.

### 오류 응답

API 오류는 동일한 형식으로 반환합니다.

```json
{
  "message": "회고를 찾을 수 없습니다: 999"
}
```

### 회고 저장 예시

```json
{
  "userId": 2,
  "lectureId": 3,
  "understood": "RAG 기본 구조와 Embedding 역할은 이해했다",
  "difficult": "Chunking 기준과 Vector Store 검색 방식이 아직 헷갈린다",
  "wantsToLearn": "Spring AI에서 실제 RAG 구현 과정을 다시 보고 싶다"
}
```

### 분석 결과 예시

```json
{
  "understandingScore": 57,
  "understoodTopics": [
    "RAG",
    "Embedding"
  ],
  "weakTopics": [
    "Chunking",
    "Vector Store"
  ],
  "levelBefore": 2,
  "levelAfter": 2,
  "reviewMaterial": {
    "reviewId": 1,
    "quizId": 1,
    "title": "Spring AI 맞춤 복습",
    "coreConcepts": [
      "Chunking",
      "Vector Store"
    ],
    "exampleCode": "...",
    "quiz": [
      {
        "conceptName": "Chunking",
        "question": "...",
        "options": [
          "..."
        ]
      }
    ]
  }
}
```

---

## 6. 인증과 접근 통제

로그인에 성공하면 Backend에서 HTTP Session을 생성하고 쿠키를 통해 사용자를 식별합니다.

클라이언트가 전달하는 `userId`를 그대로 권한 판단에 사용하지 않고 서버 세션의 사용자 정보와 비교합니다.

```text
비로그인 사용자
→ 401 Unauthorized

LEARNER
→ 본인 데이터만 조회 및 수정 가능
→ 다른 사용자 데이터 접근 시 403 Forbidden

ADMIN
→ 교육생 집계 데이터 조회 가능
→ /api/admin/** 접근 가능
```

`/api/auth/**`와 `/api/lectures/**`는 공개하며 나머지 API는 세션이 필요합니다.

비밀번호는 BCrypt를 이용해 해시한 뒤 저장합니다.

존재하지 않는 계정과 잘못된 비밀번호는 모두 동일한 `401` 응답으로 처리해 계정 존재 여부가 외부에 노출되지 않도록 했습니다.

Frontend에서는 세션 쿠키를 함께 전달합니다.

```javascript
axios.create({
  withCredentials: true
})
```

---

## 7. 실행 방법

### 1. DB 환경 변수 설정

`backend/.env` 파일을 생성합니다.

```bash
cd backend
cp .env.example .env
```

환경 변수에 DB 접속 정보를 입력합니다.

```bash
DB_URL=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.whvadgfunkpmllizweom
DB_PASSWORD=
```

DB 비밀번호는 저장소에 커밋하지 않습니다.

프로젝트 개발 환경에서는 Supabase Session Pooler를 사용했습니다.

```text
Port 5432
```

개발 환경에서 Direct Connection의 IPv6 연결 제약이 있었고, Transaction Pooler는 당시 JPA/Hibernate 설정과의 호환 문제로 사용하지 않았습니다.

또한 무료 플랜의 연결 수 제한을 고려해 애플리케이션 인스턴스당 최대 커넥션 수를 2개로 제한했습니다.

### 2. Backend 실행

```bash
cd backend
./gradlew bootRun
```

Backend는 다음 주소에서 실행됩니다.

```text
http://localhost:8080
```

`backend/.env`는 `spring.config.import`를 통해 자동으로 로드됩니다.

따라서 IntelliJ 실행 구성에 동일한 환경 변수를 별도로 등록할 필요가 없습니다.

### 3. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

Frontend는 기본적으로 다음 주소에서 실행됩니다.

```text
http://localhost:5173
```

CORS 허용 Origin 역시 `http://localhost:5173`으로 설정되어 있습니다.

5173 포트가 이미 사용 중이면 Vite가 다른 포트를 선택할 수 있으므로 기존 프로세스를 종료하거나 Backend의 CORS 설정을 함께 변경해야 합니다.

---

## 8. 데모

### 데모 계정

| 구분 | 계정 |
|---|---|
| 교육생 | `learner@skala.com` / `demo` |
| 운영자 | `admin@skala.com` / `demo` |

### 데모 강의

```text
컨테이너
Vue.js
Spring AI
AI 웹 서비스 설계
```

### 데모 데이터 초기화

시연 전 상태를 초기화하려면 다음 SQL을 실행합니다.

```text
supabase/demo-reset.sql
```

초기화 후 기본 학습 상태는 다음과 같습니다.

```text
학습 수준
Level 2 이해

학습 진도
50%
회고 2 / 강의 4

평균 이해도
47%

Docker          58
Vue             55
Kubernetes      42
Vue Router      35
```

Vue.js와 컨테이너 강의에는 초기 회고 데이터가 있습니다.

해당 강의에서 회고를 다시 작성하면 다음 제약 조건에 따라 기존 데이터가 갱신됩니다.

```text
UNIQUE(user_id, lecture_id)
```

데모 테스트에는 `Spring AI` 또는 `AI 웹 서비스 설계` 강의를 사용하는 것을 권장합니다.

### 데모 시나리오

```text
1. 로그인
   learner@skala.com / demo

2. 대시보드
   Level 2
   진도 50%
   복습이 필요한 개념 확인

3. Spring AI 강의 선택
   강의자료 확인

4. 학습 회고 작성
   "RAG는 이해했다"
   "Chunking과 Vector Store가 헷갈린다"

5. 이해도 분석
   이해한 개념: RAG, Embedding
   취약 개념: Chunking, Vector Store

6. 맞춤 복습자료 확인

7. 퀴즈 응시

8. 채점 결과 확인
   점수
   오답 해설
   개념별 이해도 변화

9. 대시보드 복귀
   진도 50% → 75%
```

회고 내용은 데모 중 직접 입력할 수 있으며 입력한 내용에 따라 분석 결과가 달라집니다.

---

## 9. 개발 과정에서 해결한 문제

### DB 커넥션 풀 한도

개발 과정에서 팀원들이 동시에 Backend를 실행하면서 DB 연결 수가 제한을 초과하는 문제가 발생했습니다.

각 개발 환경에서 여러 개의 커넥션을 유지하면서 일부 팀원의 Backend가 DB에 연결되지 않았습니다.

애플리케이션 인스턴스당 최대 커넥션을 2개로 제한하고 유휴 연결 시간을 조정해 해결했습니다.

이를 통해 애플리케이션 인스턴스 수와 DB 연결 한도를 함께 고려해야 한다는 점을 확인했습니다.

### JSONB 내부 데이터 구조 변경

초기에는 퀴즈 문항을 서술형으로 저장했지만 이후 객관식 형태로 변경했습니다.

JSONB 컬럼 타입 자체는 그대로였기 때문에 DB 스키마 검증에서는 문제가 발생하지 않았습니다.

그러나 기존 형식으로 저장된 데이터가 남아 있어 퀴즈 응시 과정에서 `500 Internal Server Error`가 발생했습니다.

JSONB를 사용하더라도 내부 데이터 구조가 변경되면 기존 데이터에 대한 마이그레이션이 필요하다는 점을 확인했습니다.

### 개념 부분 문자열 오인식

규칙 기반 분석 과정에서 `Vue`가 `RouterView`에 포함되어 있다는 이유로 라우팅 관련 회고가 `Vue` 개념으로 잘못 분류되는 문제가 있었습니다.

영문 개념은 토큰 경계를 확인하도록 수정해 다른 단어의 일부를 개념으로 잘못 인식하지 않도록 했습니다.

한글은 조사가 붙을 수 있기 때문에 별도 기준을 적용했습니다.

---

## 10. 설계에서 유지한 원칙

### Mock 데이터는 AI Adapter에서만 생성

초기에는 Frontend에도 별도의 복습자료 Mock 데이터가 존재했습니다.

이 경우 Backend의 AI 구현체를 변경하더라도 Frontend가 기존 Mock 데이터를 계속 사용할 수 있어 실제 데이터 흐름을 검증하기 어렵습니다.

Frontend의 Mock 데이터를 제거하고 `AiAnalysisPort` 뒤의 Adapter에서만 규칙 기반 결과를 생성하도록 정리했습니다.

```text
Frontend
    ↓
Backend API
    ↓
AiAnalysisPort
    ↓
MockAiAnalysisAdapter
```

이를 통해 Frontend부터 DB까지 동일한 데이터 흐름으로 연결했습니다.

### 퀴즈 결과를 학습 상태에 반영

맞춤 복습자료를 생성하는 데서 끝내지 않고 퀴즈 결과를 `concept_mastery`에 반영했습니다.

```text
회고
 ↓
AI 분석
 ↓
맞춤 복습
 ↓
퀴즈
 ↓
채점
 ↓
Concept Mastery
 ↓
대시보드
```

학습자의 입력과 평가 결과가 다시 학습 상태로 돌아가도록 구성해 하나의 학습 흐름을 완성했습니다.

---

## 11. 현재 한계

현재 AI 분석기는 **11개의 개념 사전을 기반으로 한 규칙 방식**으로 동작합니다.

따라서 사전에 정의되지 않은 새로운 개념을 자동으로 분석하기 어렵습니다.

또한 현재 분석 입력에는 강의자료 본문 전체가 아니라 강의 제목, 설명, 자료 제목이 활용됩니다.

실제 LLM과 RAG를 연결하면 다음 부분을 우선적으로 확장할 계획입니다.

```text
PDF 본문 파싱
↓
Chunking
↓
Embedding
↓
Vector Store
↓
강의별 Retrieval
↓
학습 회고
↓
LLM 분석
```

현재 세션은 애플리케이션 메모리에 저장합니다.

서버 인스턴스가 여러 대로 확장될 경우 세션을 공유할 수 없기 때문에 운영 환경에서는 Redis 기반 Session Store 또는 JWT 기반 인증 구조가 필요합니다.

---

## 12. 향후 확장

### Phase 1. 실제 LLM과 RAG 연결

```text
PDF
 ↓
Parsing
 ↓
Chunking
 ↓
Embedding
 ↓
Vector Store
 ↓
강의별 Retrieval
 ↓
Reflection Analyzer
 ↓
Review Generator
 ↓
Quiz Generator
```

주요 작업:

- `AiAnalysisPort` 실제 구현체 연결
- PDF 본문 파싱
- Chunking
- Embedding
- pgvector 기반 Vector Store
- 강의 단위 Retrieval
- RAG 기반 분석과 복습자료 생성

### Phase 2. 장기 학습 추적

- 개념별 이해도 변화 그래프
- 취약 개념 변화 추적
- 복습 주기 추천
- 운영자 상세 분석 화면
- 학습자별 장기 학습 데이터 분석

### Phase 3. 운영 환경 확장

- Redis 기반 Session Store 또는 JWT
- 컨테이너 이미지 구성
- CI/CD
- 인스턴스별 DB 커넥션 풀 관리
- 환경별 설정 분리

`feat/llm-adapter` 브랜치에는 실제 LLM 연동을 위한 초기 구현이 있습니다.

프롬프트 3종과 `OpenAiAnalysisAdapter` 구현을 진행했으며 실제 API 호출 검증은 추가로 필요합니다.

---

## 13. 프로젝트 구조

```text
ai-review-learning-platform/
├── backend/
│   └── src/main/java/com/skala/ailearning/
│       ├── ai/
│       │   └── AI 확장 지점, 규칙 기반 Adapter, 분석 결과
│       │
│       ├── common/
│       │   └── 예외 처리, CORS, Session 접근 통제
│       │
│       ├── lecture/
│       │   └── 강의, 강의자료
│       │
│       ├── mastery/
│       │   └── 개념별 학습 수준, 운영자 집계
│       │
│       ├── quiz/
│       │   └── 퀴즈, 응시 기록, 채점
│       │
│       ├── reflection/
│       │   └── 학습 회고 작성, AI 분석
│       │
│       └── user/
│           └── 사용자, 인증
│
├── frontend/
│   └── src/
│       ├── api/
│       │   └── Backend API 호출
│       │
│       ├── components/
│       │   └── 공통 레이아웃, UI 컴포넌트
│       │
│       ├── stores/
│       │   └── 로그인 상태, 학습 진행 상태
│       │
│       └── pages/
│           └── 랜딩, 로그인, 대시보드, 강의 목록,
│               회고, 분석, 회고 기록, 퀴즈
│
├── docs/
│   ├── erd.dbml
│   ├── api.http
│   └── rag-design.md
│
└── supabase/
    ├── migrations/
    └── demo-reset.sql
```

Backend는 기능 단위로 패키지를 나누고 각 기능 내부에서 Controller, Service, Repository 역할을 분리하는 구조를 사용했습니다.

---

## 14. DB Schema 관리

Supabase Dashboard에서 테이블을 직접 생성하지 않고 Migration을 통해 Schema를 관리합니다.

새 Migration 파일 생성:

```bash
supabase migration new <name>
```

원격 DB 반영:

```bash
supabase db push
```

Migration 상태 확인:

```bash
supabase migration list
```

Spring JPA 설정은 다음과 같습니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

애플리케이션에서 테이블을 자동으로 생성하지 않고 JPA Entity와 실제 DB Schema가 일치하는지만 검증합니다.

Entity와 Schema가 다르면 Backend 시작 단계에서 오류가 발생합니다.

Migration 파일은 Git에 포함하지만 다음 정보는 저장소에 올리지 않습니다.

```text
DB Password
API Key
Secret
민감한 접속 정보
```

---

## 15. 프로젝트 핵심 흐름

Memo:iry가 검증하고자 한 핵심은 단순한 AI 응답 생성이 아니라 **학습자의 상태가 다음 학습에 다시 활용되는 구조**입니다.

```text
강의자료
   ↓
학습 회고
   ↓
이해도 분석
   ↓
취약 개념
   ↓
맞춤 복습자료
   ↓
퀴즈
   ↓
채점
   ↓
Concept Mastery
   ↓
학습 상태 갱신
   ↓
다음 학습
```

> **수업이 끝나는 순간, 개인화 학습이 시작됩니다.**
