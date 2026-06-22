-- 연습용 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    passwd    VARCHAR(100) NOT NULL,
    email     VARCHAR(100),
    user_type VARCHAR(10)  NOT NULL DEFAULT 'U'  -- U: 일반, A: 관리자
);
