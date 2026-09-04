-- 데모 baseline 개념 4개의 summary 를 채운다.
-- 이 행들은 summary 컬럼이 생기기 전에 들어가서 NULL 이었다.
-- 같은 내용이 supabase/demo-reset.sql 에도 있다. 초기화해도 유지되도록 그쪽이 원본이다.



INSERT INTO concept_mastery (user_id, concept_name, level, score, summary, updated_at)
SELECT u.user_id, c.concept_name, c.level, c.score, c.summary, c.updated_at
FROM users u
CROSS JOIN LATERAL (VALUES
    ('Vue', 3, 55,
     'Vue.js Frontend Framework 강의 회고에서 이해한 개념으로 분류했다. '
     '회고 원문: "ref 와 reactive 로 상태를 만들고 화면에 연결하는 흐름은 이해했습니다."',
     TIMESTAMPTZ '2026-08-23 18:35+09'),

    ('Vue Router', 2, 35,
     'Vue.js Frontend Framework 강의 회고에서 보완이 필요한 개념으로 분류했다. '
     '회고 원문: "컴포넌트 사이에 값을 전달하는 방식과 라우팅이 아직 헷갈립니다." '
     '복습 지점: 부모 라우트의 children 배열에 자식 라우트를 등록한다 / '
     '자식 라우트의 path 앞에는 슬래시를 붙이지 않는다. 붙이면 절대 경로가 된다 / '
     '자식 컴포넌트는 부모 컴포넌트의 RouterView 자리에 그려진다 / '
     '페이지가 바뀌어도 남아야 하는 값은 컴포넌트가 아니라 store 에 둔다 / '
     'router.push 에 경로 대신 이름을 쓰면 주소가 바뀌어도 코드를 안 고쳐도 된다',
     TIMESTAMPTZ '2026-08-23 18:35+09'),

    ('Docker', 3, 58,
     '컨테이너 이해 및 애플리케이션 컨테이너화 강의 회고에서 이해한 개념으로 분류했다. '
     '회고 원문: "Docker 이미지를 빌드하고 컨테이너로 실행하는 과정은 이해했습니다."',
     TIMESTAMPTZ '2026-08-24 18:35+09'),

    ('Kubernetes', 2, 42,
     '컨테이너 이해 및 애플리케이션 컨테이너화 강의 회고에서 보완이 필요한 개념으로 분류했다. '
     '회고 원문: "Kubernetes 에서 Deployment 와 Pod 가 어떤 관계인지 잘 모르겠습니다." '
     '복습 지점: Pod 는 배포의 최소 단위이고, Deployment 가 Pod 개수와 버전을 관리한다 / '
     'Service 는 바뀌는 Pod IP 앞에 고정된 주소를 제공한다 / '
     '선언한 상태와 실제 상태를 계속 맞추는 것이 컨트롤러의 일이다',
     TIMESTAMPTZ '2026-08-24 18:35+09')
) AS c(concept_name, level, score, summary, updated_at)
WHERE u.email = 'learner@skala.com'
ON CONFLICT ON CONSTRAINT uk_user_concept DO UPDATE
    SET summary = EXCLUDED.summary;
