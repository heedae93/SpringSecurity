package com.example.authtest.service;

import com.example.authtest.dto.LoginDto;
import com.example.authtest.dto.TokenDto;

/**
 * 인증 관련 비즈니스 로직을 정의하는 인터페이스
 *
 * 역할:
 *   - 로그인 처리에 필요한 메서드를 선언한다
 *   - 구현체(AuthServiceImpl)와 호출하는 쪽(AuthController)을 분리한다
 *
 * 왜 인터페이스로 만드는가:
 *   - Controller는 AuthService 인터페이스만 알면 되고
 *     실제 구현이 어떻게 되어있는지 알 필요가 없다
 *   - 나중에 구현체를 교체하거나 테스트용 구현체로 바꿔도
 *     Controller 코드를 수정할 필요가 없다
 *   - Spring의 의존성 주입(DI)과 잘 맞는 패턴이다
 *
 * 어디서 사용되는가:
 *   - AuthController : @Autowired AuthService authService 로 주입받아서 사용
 *   - AuthServiceImpl : 이 인터페이스를 implements 해서 구현
 */
public interface AuthService {

    /**
     * 로그인 처리
     * id/passwd를 검증하고 성공 시 JWT 토큰을 발급한다
     *
     * @param loginDto 클라이언트가 보낸 로그인 정보 (id, passwd, user_type)
     * @return TokenDto JWT 토큰 정보 (accessToken, 만료시각, user_id, user_type)
     */
    TokenDto login(LoginDto loginDto);
}
