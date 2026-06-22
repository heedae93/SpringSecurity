package com.example.authtest.config.security;

import com.example.authtest.config.jwt.JwtFilter;
import com.example.authtest.config.jwt.JwtTokenProvider;
import com.example.authtest.config.security.auth.CustomAuthenticationProvider;
import com.example.authtest.config.security.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전체 설정을 담당하는 클래스
 *
 * 역할:
 *   - 지금까지 만든 모든 보안 관련 클래스들을 Spring Security에 등록하고 연결한다
 *   - 어떤 경로는 인증 없이 허용하고, 어떤 경로는 토큰이 필요한지 설정한다
 *   - JwtFilter를 FilterChain에 등록해서 모든 요청에서 JWT 검사가 이루어지게 한다
 *   - 세션을 사용하지 않는 STATELESS 방식으로 설정한다
 *
 * 왜 필요한가:
 *   - CustomAuthenticationProvider, JwtFilter 등은 Bean으로 등록되어 있어도
 *     SecurityConfig에서 명시적으로 등록하지 않으면 Spring Security가 사용하지 않는다
 *   - 이 클래스가 없으면 Spring Security는 기본 설정으로 동작한다
 *     (모든 경로 차단, 임시 비밀번호 발급, Form Login 활성화)
 *
 * @Configuration:
 *   - 이 클래스가 Spring Bean 설정 클래스임을 선언
 *   - 내부의 @Bean 메서드들이 Spring 컨테이너에 Bean으로 등록됨
 *
 * @EnableWebSecurity:
 *   - Spring Security를 활성화하고 이 클래스의 설정을 적용하겠다고 선언
 *   - 이 어노테이션이 있어야 filterChain()의 설정이 Spring Security에 반영됨
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드들을 생성자 주입으로 받는다
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Spring Security의 핵심 설정 메서드
     * FilterChainProxy 내부의 필터 체인 구성을 정의한다
     *
     * 설정 항목:
     *   1. CSRF 비활성화    : REST API는 세션을 사용하지 않아 CSRF 공격 위험이 없음
     *   2. Form Login 비활성화 : Spring Security 기본 로그인 폼을 사용하지 않음
     *   3. HTTP Basic 비활성화 : ID/PW를 매 요청마다 보내는 방식 사용하지 않음
     *   4. 세션 STATELESS   : JWT 기반이므로 서버에 세션을 저장하지 않음
     *   5. 경로별 접근 권한  : /auth/login 은 누구나, 나머지는 인증 필요
     *   6. JwtFilter 등록   : UsernamePasswordAuthenticationFilter 앞에 배치
     *
     * @param http HttpSecurity - Spring Security 설정 빌더
     * @return SecurityFilterChain - 완성된 필터 체인
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // 1. CSRF 비활성화
            //    CSRF는 세션 기반 인증에서 발생하는 공격
            //    JWT 기반(STATELESS)에서는 세션이 없으므로 불필요
            .csrf(csrf -> csrf.disable())

            // 2. Form Login 비활성화
            //    Spring Security 기본 로그인 페이지(/login)를 사용하지 않음
            //    우리가 직접 /auth/login Controller를 만들 것
            .formLogin(form -> form.disable())

            // 3. HTTP Basic 비활성화
            //    Authorization: Basic base64(id:password) 방식 사용하지 않음
            .httpBasic(basic -> basic.disable())

            // 4. 세션 STATELESS 설정
            //    서버가 세션을 생성하거나 저장하지 않음
            //    매 요청마다 JWT로만 사용자를 식별
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 5. 경로별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // /auth/login 은 토큰 없이 누구나 접근 가능 (로그인 API)
                .requestMatchers("/auth/login").permitAll()
                // 로그인 테스트 페이지
                .requestMatchers("/").permitAll()
                // h2-console 은 개발용으로 허용
                .requestMatchers("/h2-console/**").permitAll()
                // 그 외 모든 요청은 인증(JWT) 필요
                .anyRequest().authenticated()
            )

            // H2 콘솔은 iframe을 사용하므로 frameOptions 허용
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()))

            // 6. JwtFilter 등록
            //    UsernamePasswordAuthenticationFilter 앞에 JwtFilter를 배치
            //    즉, 모든 요청에서 Spring Security 기본 인증 처리 전에 JWT를 먼저 확인
            .addFilterBefore(new JwtFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화 Bean 등록
     *
     * BCryptPasswordEncoder:
     *   - BCrypt 해시 알고리즘을 사용하는 PasswordEncoder 구현체
     *   - 같은 비밀번호라도 매번 다른 해시값을 생성 (Salt 자동 포함)
     *   - matches(평문, 해시값) 으로 비교
     *
     * 어디서 사용되는가:
     *   - CustomAuthenticationProvider : 비밀번호 검증 시 matches() 호출
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CustomAuthenticationProvider Bean 등록
     *
     * Spring Security에 "비밀번호 검증은 이 클래스로 해줘" 라고 등록하는 것
     * AuthenticationManager가 authenticate() 를 호출할 때
     * supports()가 true인 이 Provider를 찾아서 실행한다
     *
     * 어디서 사용되는가:
     *   - AuthenticationManager : authenticate() 호출 시 이 Provider를 사용
     *   - AuthServiceImpl : authenticationManager.authenticate()로 인증 위임
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(customUserDetailsService, passwordEncoder());
    }

    /**
     * AuthenticationManager Bean 등록
     *
     * AuthenticationManager:
     *   - 인증 요청을 받아서 적절한 AuthenticationProvider에게 위임하는 관리자
     *   - AuthServiceImpl에서 authenticationManager.authenticate()를 호출하면
     *     등록된 CustomAuthenticationProvider.authenticate()가 실행된다
     *
     * AuthenticationConfiguration:
     *   - Spring Security가 자동으로 생성하는 AuthenticationManager를 가져오는 설정 클래스
     *   - 위에서 등록한 authenticationProvider()가 자동으로 연결됨
     *
     * @param authenticationConfiguration Spring Security 자동 설정
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
