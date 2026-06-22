package com.example.authtest.config.security.auth;

import com.example.authtest.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security가 인증에 사용하는 사용자 정보 래퍼 클래스
 *
 * 역할:
 *   - Spring Security는 사용자 정보를 UserDetails 인터페이스로만 다룬다
 *   - 우리가 만든 User 엔티티를 Spring Security가 직접 이해하지 못하므로
 *     UserDetails를 구현한 이 클래스로 포장(래핑)해서 전달해야 한다
 *
 * 왜 필요한가:
 *   - Spring Security의 AuthenticationProvider, SecurityContext 등은
 *     모두 UserDetails 타입으로 사용자를 다룬다
 *   - User 엔티티를 그대로 쓰면 Spring Security와 연동이 불가능하다
 *   - 중간 다리 역할을 하는 클래스가 바로 CustomUserDetails다
 *
 * 어디서 사용되는가:
 *   - CustomUserDetailsService : User 엔티티를 CustomUserDetails로 변환해서 반환
 *   - CustomAuthenticationProvider : authenticate() 메서드에서 비밀번호 검증 시 사용
 *   - SecurityContextHolder : 인증 성공 후 현재 사용자 정보를 저장할 때 사용
 *
 * UserDetails 인터페이스 구현 메서드들:
 *   - getUsername()              : 사용자 식별자 반환 (여기서는 userId)
 *   - getPassword()              : 저장된 암호화 비밀번호 반환 (BCrypt)
 *   - getAuthorities()           : 권한 목록 반환 (ROLE_USER, ROLE_ADMIN 등)
 *   - isAccountNonExpired()      : 계정 만료 여부 (true = 만료 안됨)
 *   - isAccountNonLocked()       : 계정 잠금 여부 (true = 잠금 안됨)
 *   - isCredentialsNonExpired()  : 비밀번호 만료 여부 (true = 만료 안됨)
 *   - isEnabled()                : 계정 활성화 여부 (true = 활성화)
 */
@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * 포장하는 User 엔티티
     * CustomAuthenticationProvider에서 user 정보가 필요할 때 getUser()로 꺼내 쓴다
     */
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Spring Security가 사용자를 식별하는 아이디 반환
     * UsernamePasswordAuthenticationToken의 principal(주체)로 사용된다
     */
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    /**
     * DB에 저장된 암호화된 비밀번호 반환
     * CustomAuthenticationProvider에서 클라이언트가 입력한 비밀번호와 비교할 때 사용된다
     */
    @Override
    public String getPassword() {
        return user.getPasswd();
    }

    /**
     * 사용자의 권한 목록 반환
     * user_type을 보고 ROLE_USER 또는 ROLE_ADMIN 권한을 부여한다
     *
     * GrantedAuthority : Spring Security의 권한을 표현하는 인터페이스
     * SimpleGrantedAuthority : GrantedAuthority의 기본 구현체. 문자열로 권한을 표현한다
     *
     * 예) user_type = "U" → [ROLE_USER]
     *     user_type = "A" → [ROLE_ADMIN]
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "A".equals(user.getUserType()) ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * 아래 4개는 계정 상태를 나타내는 메서드들이다
     * 실습에서는 모두 true(정상)로 고정한다
     * 실제 서비스에서는 DB에 상태 컬럼을 두고 관리한다
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;    // 계정 만료 안됨
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;    // 계정 잠금 안됨
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;    // 비밀번호 만료 안됨
    }

    @Override
    public boolean isEnabled() {
        return true;    // 계정 활성화
    }
}
