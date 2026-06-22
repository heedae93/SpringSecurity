package com.example.authtest.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 HTTP 요청에서 JWT를 추출하고 유효성을 검사하는 필터
 *
 * 역할:
 *   - 클라이언트가 요청 Header에 담아서 보낸 JWT를 꺼낸다
 *   - JwtTokenProvider로 토큰 유효성을 검사한다
 *   - 유효하면 토큰에서 사용자 정보를 복원해서 SecurityContextHolder에 저장한다
 *   - SecurityContextHolder에 저장된 정보로 이후 권한 체크가 이루어진다
 *
 * 왜 필요한가:
 *   - JWT 기반 인증에서는 세션을 사용하지 않는다 (STATELESS)
 *   - 매 요청마다 "이 사람이 누구인가?"를 토큰으로 확인해야 한다
 *   - 이 확인 작업을 Controller 진입 전에 필터 단계에서 처리한다
 *
 * 어디서 사용되는가:
 *   - SecurityConfig : http.addFilterBefore()로 FilterChain에 등록
 *   - 등록 후에는 모든 요청에서 자동으로 실행됨
 *
 * OncePerRequestFilter:
 *   - Spring이 제공하는 필터 추상 클래스
 *   - 하나의 요청에 대해 딱 한 번만 실행됨을 보장한다
 *   - doFilterInternal()을 오버라이드해서 필터 로직을 구현한다
 *   - umthauth에서는 BasicAuthenticationFilter를 상속했지만
 *     OncePerRequestFilter가 더 단순하고 명확하다
 *
 * Authorization Header 형식:
 *   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 *   - "Bearer " 접두사 뒤에 JWT가 온다
 *   - resolveToken()에서 "Bearer " 를 제거하고 순수한 JWT만 추출한다
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드(JwtTokenProvider)를 생성자 주입으로 받는다
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // Authorization 헤더 이름
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 토큰 접두사
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT 생성/검증/파싱 담당 클래스
     * validateToken(), getAuthentication()을 호출해서 토큰을 처리한다
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 실제 필터 로직
     * 모든 HTTP 요청이 Controller에 도달하기 전에 이 메서드가 실행된다
     *
     * 처리 흐름:
     *   1. Authorization 헤더에서 JWT 추출
     *   2. JWT 유효성 검사
     *   3. 유효하면 JWT에서 인증 정보 복원
     *   4. SecurityContextHolder에 인증 정보 저장
     *   5. 다음 필터로 요청 전달
     *
     * 토큰이 없거나 유효하지 않아도 예외를 던지지 않는다.
     * 단순히 SecurityContextHolder에 인증 정보를 저장하지 않을 뿐이다.
     * 인증 정보가 없는 상태로 요청이 계속 진행되면
     * SecurityConfig에서 설정한 권한 체크 단계에서 401/403이 반환된다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 다음 필터로 요청을 넘기는 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        // 1. Authorization 헤더에서 JWT 추출
        //    "Bearer eyJhbGci..." → "eyJhbGci..."
        String jwt = resolveToken(request);

        // 2. JWT 유효성 검사 후 SecurityContext에 저장
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

            // 3. JWT에서 인증 정보 복원 (DB 조회 없이 토큰만으로 처리)
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

            // 4. SecurityContextHolder에 인증 정보 저장
            //    이후 Controller에서 @AuthenticationPrincipal 등으로 사용자 정보를 꺼낼 수 있다
            //    SecurityConfig의 권한 체크도 여기 저장된 정보를 기반으로 한다
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 완료 - user: {}", authentication.getName());
        }

        // 5. 다음 필터로 요청 전달
        //    토큰이 없거나 유효하지 않아도 여기서 멈추지 않고 계속 진행한다
        //    권한이 필요한 경로라면 이후 AuthorizationFilter에서 막힌다
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT를 추출한다
     *
     * Authorization 헤더 형식: "Bearer eyJhbGciOiJIUzUxMiJ9..."
     * "Bearer " (7글자) 를 제거하고 순수한 JWT만 반환한다
     *
     * @param request HTTP 요청
     * @return JWT 문자열 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // 헤더가 있고 "Bearer "로 시작하면 JWT 부분만 잘라서 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열
        }

        return null;
    }
}
