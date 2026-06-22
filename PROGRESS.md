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

### ⬜ 4단계 - Spring Security 인증 구성
- `CustomUserDetails` : Spring Security가 인식할 수 있도록 User 엔티티를 래핑하는 클래스
- `CustomUserDetailsService` : DB에서 사용자를 조회해서 CustomUserDetails로 반환
- `CustomAuthenticationProvider` : 비밀번호 검증 및 인증 처리 핵심 로직

### ⬜ 5단계 - JWT 구성
- `JwtTokenProvider` : 인증 성공 후 JWT 토큰 생성 / 검증 / 파싱
- `JwtFilter` : 모든 요청에서 JWT를 추출하고 유효성 검사 후 SecurityContext에 저장

### ⬜ 6단계 - Security 설정 완성
- `SecurityConfig` : FilterChain 구성. 공개 경로 설정, JwtFilter 등록, 세션 STATELESS 설정

### ⬜ 7단계 - 로그인 API 완성
- `AuthService` / `AuthServiceImpl` : AuthenticationManager에 인증 위임 후 JWT 발급
- `AuthController` : POST /auth/login 엔드포인트. 로그인 요청 처리 및 TokenDto 반환

---

## 전체 흐름

```
POST /auth/login { id, passwd, user_type }
        ↓
AuthController
        ↓
AuthServiceImpl → AuthenticationManager.authenticate()
        ↓
CustomAuthenticationProvider
        ├── CustomUserDetailsService → UserRepository → DB 조회
        └── 비밀번호 검증 (BCrypt)
        ↓
JwtTokenProvider.makeToken()
        ↓
TokenDto 반환 { accessToken, user_id, user_type }

━━━━━━━━━━━━━━━━━━━━━━━━
이후 API 요청
━━━━━━━━━━━━━━━━━━━━━━━━

Header: Authorization: Bearer <token>
        ↓
JwtFilter → validateToken() → SecurityContext 저장
        ↓
Controller
```
