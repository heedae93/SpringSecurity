-- 테스트용 계정 (비밀번호: 1234  →  bcrypt 암호화 값)
INSERT INTO users (user_id, passwd, email, user_type)
VALUES ('user01', '$2b$10$97aXjiKmgldvmmBE4vH0cedXo2gQMBaG4KE0qXVX4wjrES1FASoyS', 'user01@test.com', 'U');

INSERT INTO users (user_id, passwd, email, user_type)
VALUES ('admin01', '$2b$10$97aXjiKmgldvmmBE4vH0cedXo2gQMBaG4KE0qXVX4wjrES1FASoyS', 'admin01@test.com', 'A');
