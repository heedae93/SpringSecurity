package com.example.authtest.controller;

import com.example.authtest.dto.LoginDto;
import com.example.authtest.dto.TokenDto;
import com.example.authtest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 HTTP 요청을 처리하는 Controller
 *
 * 역할:
 *   - 클라이언트의 로그인 요청을 받아서 AuthService에 처리를 위임한다
 *   - 처리 결과(TokenDto)를 HTTP 응답으로 클라이언트에게 반환한다
 *   - 인증 실패 시 에러 응답을 반환한다
 *
 * 왜 Controller가 마지막인가:
 *   - Controller는 아래 계층이 모두 준비된 상태에서만 의미가 있다
 *   - AuthService → AuthenticationManager → CustomAuthenticationProvider
 *     → CustomUserDetailsService → UserRepository → JwtTokenProvider
 *     이 모든 체인이 완성된 후에야 Controller가 동작할 수 있다
 *
 * @RestController:
 *   - @Controller + @ResponseBody 의 조합
 *   - 모든 메서드의 반환값이 JSON으로 직렬화되어 HTTP 응답 Body에 담긴다
 *
 * @RequestMapping("/auth"):
 *   - 이 Controller의 모든 엔드포인트 앞에 /auth 가 붙는다
 *   - POST /auth/login
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드(AuthService)를 생성자 주입으로 받는다
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 인증 처리를 담당하는 서비스
     * 인터페이스 타입으로 주입받아서 구현체(AuthServiceImpl)와 느슨하게 결합
     */
    private final AuthService authService;

    /**
     * 로그인 API
     *
     * 요청:
     *   POST /auth/login
     *   Content-Type: application/json
     *   {
     *     "id": "user01",
     *     "passwd": "1234",
     *     "user_type": "U"
     *   }
     *
     * 응답 (성공 200):
     *   {
     *     "grantType": "bearer",
     *     "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "accessTokenExpiresIn": 1750000000000,
     *     "user_id": "user01",
     *     "user_type": "U"
     *   }
     *
     * 응답 (실패 401):
     *   {
     *     "message": "아이디 또는 비밀번호가 올바르지 않습니다."
     *   }
     *
     * SecurityConfig에서 /auth/login 을 permitAll() 로 설정했으므로
     * JWT 없이 누구나 이 엔드포인트에 접근할 수 있다
     *
     * @param loginDto 로그인 요청 정보 (@RequestBody로 JSON → 객체 자동 변환)
     * @return ResponseEntity<TokenDto> JWT 토큰 정보
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            TokenDto tokenDto = authService.login(loginDto);
            return ResponseEntity.ok(tokenDto);

        } catch (Exception e) {
            // 인증 실패 (아이디 없음, 비밀번호 불일치 등)
            // CustomAuthenticationProvider에서 BadCredentialsException 발생 시 여기서 잡힘
            log.warn("로그인 실패 - id: {}, 사유: {}", loginDto.getId(), e.getMessage());
            return ResponseEntity
                    .status(401)
                    .body("{\"message\": \"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
        }
    }
}
