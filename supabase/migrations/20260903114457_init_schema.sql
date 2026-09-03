-- =========================================================
-- Memo:iry Minimal ERD  (PostgreSQL / Supabase)
-- Mini Project용 핵심 데이터 모델 — 9개 테이블
-- =========================================================
--
-- MySQL DDL 에서 옮기며 바뀐 것
--   AUTO_INCREMENT        → BIGSERIAL
--   DATETIME              → TIMESTAMPTZ
--   JSON                  → JSONB
--   컬럼 인라인 COMMENT   → COMMENT ON COLUMN (PostgreSQL 은 인라인을 지원하지 않는다)
--   UNIQUE KEY            → CONSTRAINT ... UNIQUE
--
-- 추가한 것
--   FK 컬럼 인덱스  PostgreSQL 은 MySQL 과 달리 FK 에 인덱스를 자동 생성하지 않는다
--   CHECK 제약      주석에 이미 적어 둔 허용값을 실제로 강제한다
--   DEFAULT now()   created_at 을 앱이 빠뜨려도 NOT NULL 위반이 나지 않게 한다
-- =========================================================


-- ---------------------------------------------------------
-- 1. 사용자
-- ---------------------------------------------------------
CREATE TABLE users (
    user_id    BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role  CHECK (role IN ('LEARNER', 'ADMIN'))
);

COMMENT ON TABLE  users            IS '사용자 | 교육생과 교육 운영자의 계정 정보를 관리';
COMMENT ON COLUMN users.user_id    IS '사용자 ID | 사용자를 식별하는 고유 ID';
COMMENT ON COLUMN users.email      IS '이메일 | 로그인 이메일';
COMMENT ON COLUMN users.password   IS '비밀번호 | 암호화된 로그인 비밀번호';
COMMENT ON COLUMN users.name       IS '사용자명 | 교육생 또는 운영자 이름';
COMMENT ON COLUMN users.role       IS '사용자 역할 | LEARNER: 교육생, ADMIN: 교육 운영자';
COMMENT ON COLUMN users.created_at IS '생성일시 | 사용자 생성 일시';


-- ---------------------------------------------------------
-- 2. 강의
-- ---------------------------------------------------------
CREATE TABLE lectures (
    lecture_id   BIGSERIAL    PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    lecture_date DATE         NOT NULL,
    start_time   TIME,
    end_time     TIME,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE  lectures              IS '강의 | 교육생이 수강하는 일자별 강의 정보를 관리';
COMMENT ON COLUMN lectures.lecture_id   IS '강의 ID | 강의를 식별하는 고유 ID';
COMMENT ON COLUMN lectures.title        IS '강의명 | 예: Spring AI 기초 및 RAG 구현';
COMMENT ON COLUMN lectures.description  IS '강의 설명 | 강의 내용 및 학습 목표';
COMMENT ON COLUMN lectures.lecture_date IS '강의일 | 강의가 진행된 날짜';
COMMENT ON COLUMN lectures.start_time   IS '시작시간 | 강의 시작 시간';
COMMENT ON COLUMN lectures.end_time     IS '종료시간 | 강의 종료 시간';
COMMENT ON COLUMN lectures.created_at   IS '생성일시 | 강의 등록 일시';


-- ---------------------------------------------------------
-- 3. 강의자료
-- ---------------------------------------------------------
CREATE TABLE lecture_materials (
    material_id   BIGSERIAL     PRIMARY KEY,
    lecture_id    BIGINT        NOT NULL,
    title         VARCHAR(255)  NOT NULL,
    material_type VARCHAR(20)   NOT NULL,
    file_url      VARCHAR(1000),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_material_lecture FOREIGN KEY (lecture_id)
        REFERENCES lectures (lecture_id),
    CONSTRAINT ck_material_type CHECK (material_type IN ('PDF', 'FILE', 'LINK'))
);

-- FK 조회용. PostgreSQL 은 FK 에 인덱스를 자동으로 만들지 않는다.
CREATE INDEX idx_material_lecture ON lecture_materials (lecture_id);

COMMENT ON TABLE  lecture_materials               IS '강의자료 | 강의에서 사용하는 PDF, 파일, 외부 링크를 관리';
COMMENT ON COLUMN lecture_materials.material_id   IS '강의자료 ID | 강의자료 고유 ID';
COMMENT ON COLUMN lecture_materials.lecture_id    IS '강의 ID | 자료가 속한 강의';
COMMENT ON COLUMN lecture_materials.title         IS '자료명 | PDF 또는 링크 제목';
COMMENT ON COLUMN lecture_materials.material_type IS '자료 유형 | PDF, FILE, LINK';
COMMENT ON COLUMN lecture_materials.file_url      IS '자료 경로 | 파일 URL 또는 외부 링크';
COMMENT ON COLUMN lecture_materials.created_at    IS '생성일시 | 자료 등록 일시';


-- ---------------------------------------------------------
-- 4. 학습 회고
--    users ↔ lectures 를 잇는 N:M 연결 테이블이기도 하다.
--    한 교육생이 한 강의에 회고를 하나만 쓰도록 UNIQUE 로 막는다.
-- ---------------------------------------------------------
CREATE TABLE reflections (
    reflection_id  BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL,
    lecture_id     BIGINT      NOT NULL,
    understood     TEXT,
    difficult      TEXT,
    wants_to_learn TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_reflection_user_lecture UNIQUE (user_id, lecture_id),
    CONSTRAINT fk_reflection_user FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_reflection_lecture FOREIGN KEY (lecture_id)
        REFERENCES lectures (lecture_id)
);

-- user_id 는 uk_reflection_user_lecture 의 선두 컬럼이라 별도 인덱스가 필요 없다.
-- lecture_id 는 그렇지 않으므로 만든다.
CREATE INDEX idx_reflection_lecture ON reflections (lecture_id);

COMMENT ON TABLE  reflections                IS '학습 회고 | 수업 후 교육생이 작성한 자기 학습 상태를 저장';
COMMENT ON COLUMN reflections.reflection_id  IS '학습 회고 ID | 회고 고유 ID';
COMMENT ON COLUMN reflections.user_id        IS '사용자 ID | 회고를 작성한 교육생';
COMMENT ON COLUMN reflections.lecture_id     IS '강의 ID | 회고 대상 강의';
COMMENT ON COLUMN reflections.understood     IS '잘 이해한 내용 | 교육생이 이해했다고 판단한 내용';
COMMENT ON COLUMN reflections.difficult      IS '어려운 내용 | 아직 이해가 부족하거나 헷갈리는 내용';
COMMENT ON COLUMN reflections.wants_to_learn IS '추가 학습 내용 | 추가로 공부하고 싶은 내용';
COMMENT ON COLUMN reflections.created_at     IS '작성일시 | 회고 작성 일시';


-- ---------------------------------------------------------
-- 5. AI 이해도 분석
--    AI 확장 지점 Point 1 (Reflection Analyzer) 의 결과가 들어온다.
--    지금은 Mock API 응답을 그대로 저장한다.
-- ---------------------------------------------------------
CREATE TABLE ai_analyses (
    analysis_id        BIGSERIAL   PRIMARY KEY,
    reflection_id      BIGINT      NOT NULL,
    understood_topics  JSONB,
    weak_topics        JSONB,
    recommended_topics JSONB,
    summary            TEXT,
    status             VARCHAR(20) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_analysis_reflection UNIQUE (reflection_id),
    CONSTRAINT fk_analysis_reflection FOREIGN KEY (reflection_id)
        REFERENCES reflections (reflection_id),
    CONSTRAINT ck_analysis_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE  ai_analyses                    IS 'AI 이해도 분석 | 강의자료와 회고를 분석한 이해 및 취약 영역 결과';
COMMENT ON COLUMN ai_analyses.analysis_id        IS 'AI 분석 ID | AI 분석 고유 ID';
COMMENT ON COLUMN ai_analyses.reflection_id      IS '학습 회고 ID | 분석 대상 회고';
COMMENT ON COLUMN ai_analyses.understood_topics  IS '이해 개념 | AI가 이해했다고 판단한 개념 목록';
COMMENT ON COLUMN ai_analyses.weak_topics        IS '취약 개념 | AI가 복습이 필요하다고 판단한 개념 목록';
COMMENT ON COLUMN ai_analyses.recommended_topics IS '추천 복습 개념 | 우선 복습할 개념 목록';
COMMENT ON COLUMN ai_analyses.summary            IS '분석 요약 | AI가 생성한 전체 학습 상태 요약';
COMMENT ON COLUMN ai_analyses.status             IS '분석 상태 | PENDING, COMPLETED, FAILED';
COMMENT ON COLUMN ai_analyses.created_at         IS '생성일시 | AI 분석 생성 일시';


-- ---------------------------------------------------------
-- 6. 개인 맞춤 복습
--    AI 확장 지점 Point 2 (Personalized Review Generator)
-- ---------------------------------------------------------
CREATE TABLE personalized_reviews (
    review_id    BIGSERIAL    PRIMARY KEY,
    analysis_id  BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    focus_topics JSONB,
    content      JSONB,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,

    CONSTRAINT uk_review_analysis UNIQUE (analysis_id),
    CONSTRAINT fk_review_analysis FOREIGN KEY (analysis_id)
        REFERENCES ai_analyses (analysis_id),
    CONSTRAINT ck_review_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

COMMENT ON TABLE  personalized_reviews              IS '개인 맞춤 복습 | AI가 취약 영역을 중심으로 생성한 개인별 복습 콘텐츠';
COMMENT ON COLUMN personalized_reviews.review_id    IS '맞춤 복습 ID | 개인화 복습 고유 ID';
COMMENT ON COLUMN personalized_reviews.analysis_id  IS 'AI 분석 ID | 복습 생성 기준 AI 분석';
COMMENT ON COLUMN personalized_reviews.title        IS '복습 제목 | 예: Spring AI 맞춤 복습';
COMMENT ON COLUMN personalized_reviews.focus_topics IS '복습 대상 개념 | 우선 복습해야 할 개념 목록';
COMMENT ON COLUMN personalized_reviews.content      IS '복습 콘텐츠 | 개념 설명, 예시, 복습 이유 등을 포함한 구조화 데이터';
COMMENT ON COLUMN personalized_reviews.status       IS '복습 상태 | NOT_STARTED, IN_PROGRESS, COMPLETED';
COMMENT ON COLUMN personalized_reviews.created_at   IS '생성일시 | 맞춤 복습 생성 일시';
COMMENT ON COLUMN personalized_reviews.completed_at IS '완료일시 | 복습 완료 일시';


-- ---------------------------------------------------------
-- 7. Quiz
--    AI 확장 지점 Point 3 (Quiz Generator)
-- ---------------------------------------------------------
CREATE TABLE quizzes (
    quiz_id    BIGSERIAL    PRIMARY KEY,
    review_id  BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    questions  JSONB        NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uk_quiz_review UNIQUE (review_id),
    CONSTRAINT fk_quiz_review FOREIGN KEY (review_id)
        REFERENCES personalized_reviews (review_id)
);

COMMENT ON TABLE  quizzes            IS 'Quiz | 맞춤 복습 이후 이해도를 확인하기 위한 AI 생성 Quiz';
COMMENT ON COLUMN quizzes.quiz_id    IS 'Quiz ID | Quiz 고유 ID';
COMMENT ON COLUMN quizzes.review_id  IS '맞춤 복습 ID | Quiz 생성 기준 복습';
COMMENT ON COLUMN quizzes.title      IS 'Quiz 제목 | 학습 확인 Quiz 제목';
COMMENT ON COLUMN quizzes.questions  IS 'Quiz 문항 | 문제, 보기, 정답, 해설을 포함한 JSON';
COMMENT ON COLUMN quizzes.created_at IS '생성일시 | Quiz 생성 일시';


-- ---------------------------------------------------------
-- 8. Quiz 응시
--    users ↔ quizzes 를 잇는 N:M 연결 테이블. 재응시를 허용하므로 UNIQUE 를 걸지 않는다.
-- ---------------------------------------------------------
CREATE TABLE quiz_attempts (
    attempt_id    BIGSERIAL   PRIMARY KEY,
    quiz_id       BIGINT      NOT NULL,
    user_id       BIGINT      NOT NULL,
    answers       JSONB,
    score         INTEGER,
    correct_count INTEGER     NOT NULL DEFAULT 0,
    total_count   INTEGER     NOT NULL DEFAULT 0,
    completed_at  TIMESTAMPTZ,

    CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id)
        REFERENCES quizzes (quiz_id),
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_attempt_score CHECK (score IS NULL OR score BETWEEN 0 AND 100)
);

CREATE INDEX idx_attempt_quiz ON quiz_attempts (quiz_id);
CREATE INDEX idx_attempt_user ON quiz_attempts (user_id);

COMMENT ON TABLE  quiz_attempts               IS 'Quiz 응시 | 교육생의 Quiz 답안과 최종 결과를 저장';
COMMENT ON COLUMN quiz_attempts.attempt_id    IS 'Quiz 응시 ID | Quiz 응시 고유 ID';
COMMENT ON COLUMN quiz_attempts.quiz_id       IS 'Quiz ID | 사용자가 응시한 Quiz';
COMMENT ON COLUMN quiz_attempts.user_id       IS '사용자 ID | Quiz를 응시한 교육생';
COMMENT ON COLUMN quiz_attempts.answers       IS '사용자 답안 | 문항별 선택 답안과 정답 여부';
COMMENT ON COLUMN quiz_attempts.score         IS 'Quiz 점수 | 0~100';
COMMENT ON COLUMN quiz_attempts.correct_count IS '정답 수 | 맞힌 문항 개수';
COMMENT ON COLUMN quiz_attempts.total_count   IS '전체 문항 수 | 전체 Quiz 문항 개수';
COMMENT ON COLUMN quiz_attempts.completed_at  IS '완료일시 | Quiz 제출 완료 일시';


-- ---------------------------------------------------------
-- 9. 개념별 학습 수준
--    회고 분석과 Quiz 결과가 최종적으로 수렴하는 곳.
--    level 은 score 에서 파생되는 값이다 — 두 척도를 따로 관리하지 않는다.
--      0-25 인지 · 26-50 이해 · 51-80 적용 · 81-100 구현
-- ---------------------------------------------------------
CREATE TABLE concept_mastery (
    mastery_id   BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    concept_name VARCHAR(150) NOT NULL,
    level        INTEGER      NOT NULL,
    score        INTEGER,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uk_user_concept UNIQUE (user_id, concept_name),
    CONSTRAINT fk_mastery_user FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT ck_mastery_level CHECK (level BETWEEN 1 AND 4),
    CONSTRAINT ck_mastery_score CHECK (score IS NULL OR score BETWEEN 0 AND 100)
);

-- user_id 는 uk_user_concept 의 선두 컬럼이라 별도 인덱스가 필요 없다.

COMMENT ON TABLE  concept_mastery              IS '개념별 학습 수준 | 교육생의 개념별 현재 Learning Level과 이해도를 관리';
COMMENT ON COLUMN concept_mastery.mastery_id   IS '학습 수준 ID | 개념별 학습 수준 고유 ID';
COMMENT ON COLUMN concept_mastery.user_id      IS '사용자 ID | 학습 수준 대상 교육생';
COMMENT ON COLUMN concept_mastery.concept_name IS '개념명 | 예: RAG, Embedding, Chunking, Vector Store';
COMMENT ON COLUMN concept_mastery.level        IS '학습 수준 | 1: 인지, 2: 이해, 3: 적용, 4: 구현';
COMMENT ON COLUMN concept_mastery.score        IS '이해도 점수 | AI 분석과 Quiz 결과를 반영한 0~100 점수';
COMMENT ON COLUMN concept_mastery.updated_at   IS '수정일시 | 개념 학습 수준 최종 갱신 일시';
