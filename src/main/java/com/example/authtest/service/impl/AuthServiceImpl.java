package com.example.authtest.service.impl;

import com.example.authtest.config.jwt.JwtTokenProvider;
import com.example.authtest.dto.LoginDto;
import com.example.authtest.dto.TokenDto;
import com.example.authtest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * AuthService 구현체. 실제 로그인 처리 로직을 담당하는 클래스
 *
 * 역할:
 *   - 클라이언트가 보낸 id/passwd로 Spring Security 인증을 요청한다
 *   - 인증 성공 시 JwtTokenProvider로 JWT를 발급해서 반환한다
 *
 * 핵심 포인트:
 *   - 이 클래스는 비밀번호를 직접 검증하지 않는다
 *   - authenticationManager.authenticate()에 인증을 위임한다
 *   - AuthenticationManager가 내부적으로 CustomAuthenticationProvider를 실행해서
 *     DB 조회와 비밀번호 검증을 처리한다
 *   - 이 클래스는 그 결과만 받아서 JWT를 발급하는 역할만 한다
 *
 * 전체 흐름:
 *   AuthController.login()
 *           ↓ authService.login(loginDto) 호출
 *   AuthServiceImpl.login()
 *           ↓ UsernamePasswordAuthenticationToken 생성 (인증 전 상태)
 *           ↓ authenticationManager.authenticate(token) 호출
 *   AuthenticationManager
 *           ↓ CustomAuthenticationProvider.authenticate() 실행
 *   CustomAuthenticationProvider
 *           ↓ CustomUserDetailsService → DB 조회
 *           ↓ passwordEncoder.matches() → 비밀번호 검증
 *           ↓ 성공 → 인증 완료된 Authentication 반환
 *   AuthServiceImpl
 *           ↓ jwtTokenProvider.generateToken(authentication) 호출
 *   JwtTokenProvider
 *           ↓ JWT 생성 → TokenDto 반환
 *   AuthServiceImpl → AuthController → 클라이언트
 *
 * @Service:
 *   - Spring Bean으로 등록. AuthController에서 주입받아 사용
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드들을 생성자 주입으로 받는다
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 인증 처리를 담당하는 Spring Security 핵심 컴포넌트
     * authenticate() 를 호출하면 등록된 CustomAuthenticationProvider가 실행된다
     * SecurityConfig에서 Bean으로 등록했다
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT 생성을 담당하는 클래스
     * 인증 성공 후 generateToken()으로 JWT를 발급한다
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 처리 메서드
     *
     * 1. loginDto에서 id/passwd를 꺼내서 UsernamePasswordAuthenticationToken 생성
     *    - 이 토큰은 "아직 인증되지 않은" 상태의 요청 객체다
     *    - principal(주체) = id, credentials(증명) = passwd
     *
     * 2. authenticationManager.authenticate(token) 호출
     *    - Spring Security가 CustomAuthenticationProvider를 찾아서 실행
     *    - 내부에서 DB 조회 + 비밀번호 검증이 이루어짐
     *    - 실패 시 BadCredentialsException 발생 → Controller에서 처리
     *
     * 3. 인증 성공 시 Authentication 객체 반환
     *    - 이 객체에 CustomUserDetails(사용자 정보)와 권한 목록이 담겨있다
     *
     * 4. JWT 발급
     *    - Authentication 객체를 JwtTokenProvider에 넘겨서 JWT를 생성한다
     *    - JWT Payload에 user_id, user_type, roles, 만료시각이 담긴다
     *
     * @param loginDto 클라이언트가 보낸 로그인 정보
     * @return TokenDto JWT 토큰 정보
     */
    @Override
    public TokenDto login(LoginDto loginDto) {

        // 1. 인증 요청 토큰 생성 (아직 인증 안된 상태)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getId(), loginDto.getPasswd());

        // 2. 인증 처리 위임
        //    CustomAuthenticationProvider.authenticate()가 실행됨
        //    실패 시 BadCredentialsException → Controller로 전파
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. JWT 발급
        return jwtTokenProvider.generateToken(authentication);
    }
}
