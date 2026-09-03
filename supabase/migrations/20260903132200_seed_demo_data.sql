INSERT INTO users (email, password, name, role) VALUES
    ('admin@skala.com',   'demo', '교육 운영자', 'ADMIN'),
    ('learner@skala.com', 'demo', '권정',       'LEARNER');

INSERT INTO lectures (title, description, lecture_date, start_time, end_time) VALUES
    ('컨테이너 이해 및 애플리케이션 컨테이너화',
     'Docker 이미지 빌드부터 Kubernetes 배포까지 컨테이너 기반 애플리케이션 운영을 다룹니다.',
     DATE '2026-08-24', TIME '09:00', TIME '18:00'),
    ('Vue.js Frontend Framework',
     '컴포넌트 구조와 상태 관리, Vite 기반 개발 환경을 다룹니다.',
     DATE '2026-08-23', TIME '09:00', TIME '18:00'),
    ('Spring AI',
     'ChatClient, RAG, Tool Calling, 프롬프트 인젝션 방어까지 Spring AI 전반을 다룹니다.',
     DATE '2026-08-31', TIME '09:00', TIME '18:00'),
    ('AI 웹 서비스 설계',
     'AI-Ready 아키텍처, Mock API 기반 설계, 데이터 모델링과 REST API 명세를 다룹니다.',
     DATE '2026-09-02', TIME '09:00', TIME '18:00');

INSERT INTO lecture_materials (lecture_id, title, material_type, file_url)
SELECT l.lecture_id, m.title, m.material_type, m.file_url
FROM lectures l
JOIN (VALUES
    ('컨테이너 이해 및 애플리케이션 컨테이너화', '컨테이너 개요 및 Docker 실습', 'PDF',  '/materials/container-intro.pdf'),
    ('컨테이너 이해 및 애플리케이션 컨테이너화', 'Kubernetes 배포 실습 가이드',  'PDF',  '/materials/k8s-deploy.pdf'),
    ('Vue.js Frontend Framework',              'Vue 컴포넌트와 상태 관리',      'PDF',  '/materials/vue-basics.pdf'),
    ('Spring AI',                              'Spring AI 교재',                'PDF',  '/materials/spring-ai.pdf'),
    ('Spring AI',                              'RAG 실습 코드',                 'LINK', 'https://github.com/kjung0109/skala-spring-ai-practice'),
    ('AI 웹 서비스 설계',                       'Mini Project Guide',            'PDF',  '/materials/mini-project-guide.pdf')
) AS m(lecture_title, title, material_type, file_url)
  ON l.title = m.lecture_title;
