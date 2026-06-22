package com.example.authtest.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * DB에서 조회한 사용자 정보를 담는 DTO (Data Transfer Object)
 *
 * 역할:
 *   - DB의 users 테이블에서 조회한 결과를 Java 객체로 담는 그릇
 *   - 이후 비밀번호 검증, JWT 생성, Spring Security 인증 처리에 모두 사용됨
 *
 * 왜 필요한가:
 *   - DB 조회 결과(ResultSet)를 그대로 쓸 수 없으므로 Java 객체로 변환이 필요
 *   - JPA Entity(User.java)와 분리된 이유:
 *     Entity는 DB 테이블과 1:1로 매핑되는 클래스이고,
 *     DTO는 계층 간 데이터 전달을 위한 클래스다.
 *     Entity를 직접 Controller나 Service에 노출하면 DB 구조가 외부에 노출되고,
 *     불필요한 데이터까지 전달될 수 있어 분리하는 것이 좋다.
 *
 * 어디서 사용되는가:
 *   - CustomUserDetailsService : UserRepository로 조회한 User(Entity)를 UserDto로 변환 후 사용
 *   - CustomAuthenticationProvider : 비밀번호 검증 시 UserDto.getPasswd() 사용
 *   - JwtTokenProvider : JWT payload에 user_id, user_type 등을 담을 때 사용
 *
 * roles 필드:
 *   - Spring Security는 사용자의 권한을 List<String> 형태로 관리한다
 *   - 예) 일반 사용자 → ["ROLE_USER"], 관리자 → ["ROLE_ADMIN"]
 *   - 로그인 시 user_type을 보고 addRole()로 권한을 부여한다
 *   - new ArrayList<>()로 미리 초기화하지 않으면 addRole() 호출 시 NullPointerException 발생
 *
 * @Data (lombok):
 *   - 컴파일 시점에 모든 필드의 getter/setter, toString, equals, hashCode를 자동 생성
 *   - 직접 작성할 필요 없음
 */
@Data
public class UserDto {

    // DB users 테이블의 컬럼과 동일한 이름으로 선언
    private String user_id;     // 사용자 ID (PK)
    private String passwd;      // 암호화된 비밀번호 (BCrypt)
    private String email;       // 이메일
    private String user_type;   // 사용자 유형: U(일반), A(관리자)

    // Spring Security가 권한 체크에 사용하는 권한 목록
    // DB 컬럼이 아니라 로그인 시점에 user_type을 보고 직접 세팅한다
    private List<String> roles = new ArrayList<>();

    /**
     * 권한을 추가하는 메서드
     * 예) userDto.addRole("ROLE_USER")
     *     userDto.addRole("ROLE_ADMIN")
     */
    public void addRole(String role) {
        this.roles.add(role);
    }
}
