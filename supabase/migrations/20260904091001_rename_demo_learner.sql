-- 데모 교육생 이름을 실명에서 과정명으로 바꾼다.
-- 시연 화면과 발표 자료에 개인 이름이 노출되지 않게 한다.

UPDATE users
SET name = '스칼라'
WHERE email = 'learner@skala.com';
