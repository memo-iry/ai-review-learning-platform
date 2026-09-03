-- 비밀번호를 평문에서 BCrypt 해시로 바꾼다.
-- 두 계정 모두 평문은 'demo' 이며, 데모용 값이다.
-- 운영에서는 가입 시 애플리케이션이 해시해 저장한다.

UPDATE users
SET password = '$2y$10$6DxQhGOBOHOpAEkKfobOz.y9mYResf/.SVPeLgv/kPfn6LfrSv.6y'
WHERE email IN ('admin@skala.com', 'learner@skala.com');
