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

### ✅ 4단계 - Spring Security 인증 구성
- `CustomUserDetails` : UserDetails 구현체. User 엔티티를 Spring Security가 이해하는 형태로 포장
- `CustomUserDetailsService` : UserDetailsService 구현체. DB에서 사용자 조회 후 CustomUserDetails로 변환
- `CustomAuthenticationProvider` : AuthenticationProvider 구현체. 비밀번호 검증 및 인증 처리 핵심 로직

### ✅ 5단계 - JWT 구성
- `JwtTokenProvider` : 인증 성공 후 JWT 생성 / 요청마다 JWT 검증 / JWT에서 사용자 정보 복원
- `JwtFilter` : 모든 요청에서 Authorization 헤더의 JWT를 추출해 검증 후 SecurityContext에 저장

