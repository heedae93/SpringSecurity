-- 테스트용 계정 (비밀번호: 1234  →  bcrypt 암호화 값)
INSERT INTO users (user_id, passwd, email, user_type)
VALUES ('user01', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa/8/0gkRsxE0kZjElUJJIMuTMkOVj8W', 'user01@test.com', 'U');

INSERT INTO users (user_id, passwd, email, user_type)
VALUES ('admin01', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa/8/0gkRsxE0kZjElUJJIMuTMkOVj8W', 'admin01@test.com', 'A');
