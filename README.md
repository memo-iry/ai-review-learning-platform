# AI 맞춤형 복습 학습 서비스

IT Full Stack 강의자료와 학습자가 작성한 회고록을 함께 활용해 이해도를 분석하고,
개인별 복습자료를 만들어 주는 3일 미니 프로젝트용 기본 구조입니다.

현재 AI 기능은 `MockAiReviewService`로 동작합니다. 화면과 API 흐름을 먼저 완성한 뒤
Spring AI 또는 별도 RAG 서버로 교체할 수 있도록 `AiReviewService` 인터페이스를 분리했습니다.

## 핵심 흐름

```text
강의자료 조회
→ 회고록 작성
→ 이해도 분석
→ 강의자료 Mock RAG 검색
→ 맞춤형 복습자료 확인
→ 복습 완료
→ 차원·레벨 성장
```

## 기술 스택

- Backend: Java 21, Spring Boot 3.3, Spring Data JPA, Flyway, Gradle
- Frontend: React 18, Vite 5
- Database: PostgreSQL 16
- Local test: H2
- Infrastructure: Docker Compose

## 폴더 구조

```text
ai-review-learning-platform/
├── backend/
│   ├── src/main/java/com/g129/ailearning/
│   │   ├── ai/           # Mock AI/RAG 및 분석 API
│   │   ├── common/       # 공통 예외 처리
│   │   ├── config/       # CORS 설정
│   │   ├── course/       # 교육과정과 수강
│   │   ├── material/     # 강의자료
│   │   ├── reflection/   # 회고록
│   │   ├── role/         # 권한
│   │   └── user/         # 사용자
│   └── src/main/resources/db/migration/ # DB 생성 및 샘플 데이터
├── frontend/
│   └── src/
│       ├── api/          # Backend API 호출
│       ├── components/   # 공통 레이아웃
│       └── pages/        # 대시보드·회고록·분석·복습·성장 화면
├── docs/erd.dbml         # dbdiagram.io용 ERD
└── docker-compose.yml
```

## 가장 빠른 실행 방법

Docker Desktop이 실행 중인 상태에서 프로젝트 루트에서 실행합니다.

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- PostgreSQL: localhost:5432

종료할 때는 다음 명령을 사용합니다.

```bash
docker compose down
```

DB 데이터까지 초기화하려면 다음 명령을 사용합니다.

```bash
docker compose down -v
```

## 로컬 개발 실행

### 1. PostgreSQL만 실행

```bash
docker compose up -d postgres
```

### 2. Backend 실행

Java 21 환경에서 실행합니다.

```bash
cd backend
./gradlew bootRun
```

Windows에서는 다음 명령을 사용합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

### 3. Frontend 실행

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

## 데모 데이터

Flyway가 서버 시작 시 다음 데이터를 자동으로 생성합니다.

| 구분 | 값 |
|---|---|
| 강사 사용자 | `instructor` |
| 학습자 사용자 | `learner` |
| 데모 학습자 ID | `2` |
| 교육과정 | `IT Full Stack` |
| 강의자료 | `Spring Boot REST API`, `React 상태 관리` |

`password` 값은 DB 구조 확인을 위한 예시입니다. 실제 로그인 기능을 구현할 때는
Spring Security와 BCrypt를 적용해야 합니다.

## 주요 API

Backend 실행 후 [Swagger UI](http://localhost:8080/swagger-ui.html)에서 전체 API 명세를 확인하고 직접 요청할 수 있습니다.

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/courses` | 교육과정 목록 조회 |
| GET | `/api/courses/{courseId}/documents` | 과정별 강의자료 조회 |
| POST | `/api/reflections` | 회고록 등록 |
| GET | `/api/reflections/{reflectionId}` | 회고록 상세 조회 |
| POST | `/api/reflections/{reflectionId}/analyze` | Mock 이해도 분석 및 복습자료 생성 |

회고록 등록 예시:

```json
{
  "userId": 2,
  "courseId": 1,
  "understoodContent": "Controller에서 요청을 받는 흐름은 이해했습니다.",
  "difficultContent": "Service와 Repository의 역할 구분이 아직 어렵습니다.",
  "questionContent": "DTO는 어느 계층에서 변환해야 하나요?"
}
```

## 실제 AI/RAG로 교체하는 위치

1. `AiReviewService`를 구현하는 새 클래스를 만듭니다.
2. 강의자료를 임베딩하고 Vector Store에 저장합니다.
3. 회고록의 어려운 내용을 검색 질의로 사용합니다.
4. 검색된 강의자료와 회고록을 LLM 프롬프트에 함께 전달합니다.
5. 새 구현체에 `@Primary`를 적용하고 `MockAiReviewService`의 `@Primary`를 제거합니다.

핵심 교체 지점:

```text
backend/src/main/java/com/g129/ailearning/ai/AiReviewService.java
backend/src/main/java/com/g129/ailearning/ai/MockAiReviewService.java
```

## GitHub 업로드

GitHub에서 빈 저장소를 만든 뒤 프로젝트 루트에서 실행합니다.

```bash
git init
git add .
git commit -m "feat: initialize AI review learning platform"
git branch -M main
git remote add origin https://github.com/사용자명/저장소명.git
git push -u origin main
```

`.env` 파일과 DB 데이터는 `.gitignore`에 포함되므로 GitHub에 올라가지 않습니다.
