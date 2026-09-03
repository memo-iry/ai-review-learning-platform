-- 확인 문제를 4지선다로 바꾸면서 quizzes.questions 의 JSON 형식이 달라졌다.
-- 구형식 {question, answer} 행은 채점 시 answerIndex 가 null 이라 500 이 난다.
-- 전부 개발 중 생성된 데이터라 지우고 데모 baseline 을 넣는다.
-- 같은 내용이 supabase/demo-reset.sql 에도 있다. 시연 직전 초기화용이다.

DELETE FROM quiz_attempts;
DELETE FROM quizzes;
DELETE FROM personalized_reviews;
DELETE FROM ai_analyses;
DELETE FROM reflections;
DELETE FROM concept_mastery;

-- 지난 강의에서 이미 회고를 작성한 상태 (진도 2/4 = 50%)
INSERT INTO reflections (user_id, lecture_id, understood, difficult, wants_to_learn, created_at)
SELECT u.user_id, l.lecture_id, r.understood, r.difficult, r.wants_to_learn, r.created_at
FROM users u
CROSS JOIN LATERAL (VALUES
    ('Vue.js Frontend Framework',
     'ref 와 reactive 로 상태를 만들고 화면에 연결하는 흐름은 이해했습니다.',
     '컴포넌트 사이에 값을 전달하는 방식과 라우팅이 아직 헷갈립니다.',
     'Pinia 같은 상태 관리 도구를 써보고 싶습니다.',
     TIMESTAMPTZ '2026-08-23 18:30+09'),
    ('컨테이너 이해 및 애플리케이션 컨테이너화',
     'Docker 이미지를 빌드하고 컨테이너로 실행하는 과정은 이해했습니다.',
     'Kubernetes 에서 Deployment 와 Pod 가 어떤 관계인지 잘 모르겠습니다.',
     '실제 배포 파이프라인을 따라가 보고 싶습니다.',
     TIMESTAMPTZ '2026-08-24 18:30+09')
) AS r(lecture_title, understood, difficult, wants_to_learn, created_at)
JOIN lectures l ON l.title = r.lecture_title
WHERE u.email = 'learner@skala.com';

-- 그 회고에서 쌓인 개념별 이해도
-- 평균 47 → Level 2(이해). 시연에서 Quiz 를 맞히면 개념 단위로 올라간다.
INSERT INTO concept_mastery (user_id, concept_name, level, score, updated_at)
SELECT u.user_id, c.concept_name, c.level, c.score, c.updated_at
FROM users u
CROSS JOIN LATERAL (VALUES
    ('Vue',         3, 55, TIMESTAMPTZ '2026-08-23 18:35+09'),
    ('Vue Router',  2, 35, TIMESTAMPTZ '2026-08-23 18:35+09'),
    ('Docker',      3, 58, TIMESTAMPTZ '2026-08-24 18:35+09'),
    ('Kubernetes',  2, 42, TIMESTAMPTZ '2026-08-24 18:35+09')
) AS c(concept_name, level, score, updated_at)
WHERE u.email = 'learner@skala.com';
