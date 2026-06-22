package com.example.authtest.config.security.auth;

import com.example.authtest.entity.User;
import com.example.authtest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * DB에서 사용자를 조회해서 Spring Security에 전달하는 서비스
 *
 * 역할:
 *   - Spring Security의 UserDetailsService 인터페이스를 구현한 클래스
 *   - 로그인 시 입력된 id로 DB를 조회하고, 결과를 CustomUserDetails로 변환해서 반환한다
 *   - Spring Security의 인증 과정에서 자동으로 호출된다
 *
 * 왜 필요한가:
 *   - Spring Security는 인증 처리 시 "이 사람이 DB에 있는가?"를 확인해야 한다
 *   - 이 확인 작업을 UserDetailsService.loadUserByUsername()에 위임한다
 *   - 우리가 이 메서드를 구현해서 DB 조회 로직을 넣어주면
 *     Spring Security가 알아서 호출해서 사용한다
 *
 * 어디서 사용되는가:
 *   - CustomAuthenticationProvider : authenticate() 메서드에서
 *     loadUserByUsername()을 호출해서 DB에서 사용자를 가져온 뒤 비밀번호를 검증한다
 *
 * 흐름:
 *   CustomAuthenticationProvider.authenticate()
 *           ↓ loadUserByUsername("user01") 호출
 *   CustomUserDetailsService
 *           ↓ UserRepository.findByUserId("user01")
 *   DB 조회
 *           ↓ User 엔티티 반환
 *   new CustomUserDetails(user) 로 포장해서 반환
 *           ↓
 *   CustomAuthenticationProvider 에서 비밀번호 검증
 *
 * @Service:
 *   - Spring Bean으로 등록. 다른 클래스에서 @Autowired 또는 생성자 주입으로 사용 가능
 *
 * @RequiredArgsConstructor (lombok):
 *   - final 필드를 파라미터로 받는 생성자를 자동 생성
 *   - UserRepository를 생성자 주입 방식으로 받기 위해 사용
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * DB에서 사용자를 조회하는 JPA Repository
     * @RequiredArgsConstructor 에 의해 생성자 주입된다
     */
    private final UserRepository userRepository;

    /**
     * 사용자 아이디로 DB를 조회해서 UserDetails를 반환한다
     * Spring Security가 인증 과정에서 자동으로 호출하는 메서드
     *
     * @param username 로그인 시 입력한 사용자 아이디
     * @return CustomUserDetails (UserDetails 구현체)
     * @throws UsernameNotFoundException DB에 해당 사용자가 없을 때 발생
     *         → Spring Security가 이 예외를 받아서 인증 실패로 처리한다
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return new CustomUserDetails(user);
    }
}
