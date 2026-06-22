# Spring Security + JWT 인증 구현 학습

## 개요
Spring Boot 3.x + Spring Security + JWT 조합으로 인증 서버를 처음부터 구현하는 학습 프로젝트

---

## 구현 순서

### ✅ 1단계 - 프로젝트 초기 세팅
- `build.gradle` : Spring Boot 3.2.5, Spring Security, JWT, JPA, H2 의존성 구성
- `application.yml` : 서버 포트, H2 DB, JPA 설정
- `schema.sql` : users 테이블 생성 스크립트
- `data.sql` : 테스트용 계정 데이터 (user01, admin01)
- `AuthtestApplication.java` : Spring Boot 진입점

### ✅ 2단계 - 도메인 DTO 정의
데이터를 담는 그릇 역할. 이후 모든 클래스에서 참조됨

- `UserDto` : DB 조회 결과를 담는 객체 (user_id, passwd, email, user_type, roles)
- `LoginDto` : 클라이언트 로그인 요청을 담는 객체 (id, passwd, user_type)
- `TokenDto` : 로그인 성공 시 클라이언트에 반환할 JWT 토큰 정보 (accessToken 등)

### ✅ 3단계 - DB 접근 계층 구성
- `User` (Entity) : DB users 테이블과 매핑되는 JPA 엔티티
- `UserRepository` : JPA Repository. findByUserId()로 사용자 조회

