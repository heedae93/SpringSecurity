package com.example.authtest.config.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 실제 인증(비밀번호 검증) 로직을 처리하는 핵심 클래스
 *
 * 역할:
 *   - Spring Security의 AuthenticationProvider 인터페이스를 구현한 클래스
 *   - AuthService에서 authenticationManager.authenticate()를 호출하면
 *     Spring Security가 이 클래스의 authenticate()를 실행한다
 *   - DB에서 사용자를 조회하고, 입력한 비밀번호와 DB의 암호화된 비밀번호를 비교한다
 *
 * 왜 필요한가:
 *   - Spring Security의 기본 인증 방식(Form Login)을 사용하지 않고
 *     JWT 기반의 커스텀 인증을 구현하기 위해 직접 AuthenticationProvider를 구현한다
 *   - 비밀번호 검증 방식, 사용자 유형 분기 등 커스텀 로직을 여기서 처리한다
 *
 * 인증 흐름:
 *   AuthServiceImpl.login()
 *           ↓ authenticationManager.authenticate(token) 호출
 *   AuthenticationManager
 *           ↓ supports()로 처리 가능한 Provider 탐색
 *   CustomAuthenticationProvider.authenticate()
 *           ↓ loadUserByUsername()으로 DB에서 사용자 조회
 *   CustomUserDetailsService
 *           ↓ CustomUserDetails 반환
 *   CustomAuthenticationProvider
 *           ↓ passwordEncoder.matches()로 비밀번호 검증
 *           ↓ 성공 → UsernamePasswordAuthenticationToken 반환 (인증 완료 상태)
 *           ↓ 실패 → BadCredentialsException 발생 → 401 응답
 *
 * 어디서 사용되는가:
 *   - SecurityConfig : authenticationProvider()로 Bean 등록
 *   - AuthenticationManager : 등록된 Provider 중 supports()가 true인 것을 찾아 호출
 *
 * @Slf4j (lombok):
 *   - log.debug(), log.error() 등 로그를 출력할 수 있는 logger를 자동 생성
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드를 파라미터로 받는 생성자를 자동 생성 (생성자 주입)
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    /**
     * DB에서 사용자를 조회하는 서비스
     * loadUserByUsername()을 호출해서 CustomUserDetails를 가져온다
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * 비밀번호 암호화/검증 도구
     * BCrypt 알고리즘을 사용하며 SecurityConfig에서 Bean으로 등록한다
     * matches(평문비밀번호, 암호화된비밀번호) → true/false 반환
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 실제 인증 처리 메서드
     * AuthenticationManager가 이 메서드를 호출해서 인증을 수행한다
     *
     * @param authentication AuthServiceImpl에서 생성한 UsernamePasswordAuthenticationToken
     *                       - getPrincipal()   : 사용자가 입력한 id
     *                       - getCredentials() : 사용자가 입력한 비밀번호 (평문)
     * @return 인증 성공 시 UsernamePasswordAuthenticationToken (인증 완료 상태)
     *         - principal   : CustomUserDetails 객체
     *         - credentials : 비밀번호
     *         - authorities : 권한 목록 (ROLE_USER, ROLE_ADMIN)
     * @throws AuthenticationException 인증 실패 시 발생
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 1. 클라이언트가 입력한 id, 비밀번호 추출
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        log.debug("인증 시도 - username: {}", username);

        // 2. DB에서 사용자 조회
        //    사용자가 없으면 UsernameNotFoundException 발생 → 인증 실패
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        // 3. 비밀번호 검증
        //    passwordEncoder.matches(입력한 평문 비밀번호, DB의 BCrypt 암호화 비밀번호)
        //    BCrypt는 같은 문자열이라도 매번 다른 해시값을 만들기 때문에 == 비교가 불가능하다
        //    반드시 matches()를 사용해야 한다
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.debug("비밀번호 불일치 - username: {}", username);
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        log.debug("인증 성공 - username: {}", username);

        // 4. 인증 성공 → 인증 완료된 토큰 반환
        //    3번째 파라미터로 authorities(권한 목록)를 넣으면 "인증 완료" 상태가 된다
        //    이 토큰이 SecurityContextHolder에 저장되어 이후 인증 정보로 사용된다
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                password,
                userDetails.getAuthorities()
        );
    }

    /**
     * 이 Provider가 처리할 수 있는 Authentication 타입을 선언
     * AuthenticationManager가 여러 Provider 중 적합한 것을 찾을 때 이 메서드를 호출한다
     *
     * UsernamePasswordAuthenticationToken : id/비밀번호 기반 인증 토큰
     * → AuthServiceImpl에서 이 타입으로 토큰을 만들어서 전달하므로 true를 반환
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
