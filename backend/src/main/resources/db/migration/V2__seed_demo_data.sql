INSERT INTO roles (role_name) VALUES ('LEARNER'), ('INSTRUCTOR'), ('ADMIN');

INSERT INTO users (role_id, username, password)
VALUES
    (2, 'instructor', '{noop}demo1234'),
    (1, 'learner', '{noop}demo1234');

INSERT INTO course (instructor_id, course_name, course_description)
VALUES
    (1, 'IT Full Stack', 'Spring Boot와 React를 활용한 Full Stack 교육과정');

INSERT INTO enrollment (users_id, course_id, enrollment_status)
VALUES
    (2, 1, 'ACTIVE');

INSERT INTO document (course_id, document_name, document_text, file_url)
VALUES
    (1, 'Spring Boot REST API',
     'REST API는 자원을 URI로 표현하고 HTTP 메서드로 행위를 구분한다. Controller는 요청을 받고 Service는 비즈니스 로직을 수행한다. Repository는 데이터베이스 접근을 담당한다.',
     '/materials/spring-rest-api.pdf'),
    (1, 'React 상태 관리',
     'React의 상태는 컴포넌트 화면에 영향을 주는 데이터다. useState로 지역 상태를 관리하고 props를 통해 하위 컴포넌트에 데이터를 전달할 수 있다.',
     '/materials/react-state.pdf');

