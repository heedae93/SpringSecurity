package com.example.authtest.repository;

import com.example.authtest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * users 테이블에 접근하는 JPA Repository 인터페이스
 *
 * 역할:
 *   - DB의 users 테이블에 대한 CRUD(조회/저장/수정/삭제) 기능을 제공
 *   - 이 인터페이스를 선언하는 것만으로 Spring Data JPA가 구현체를 자동으로 생성해준다
 *     (직접 SQL을 작성하거나 구현 클래스를 만들 필요 없음)
 *
 * 왜 필요한가:
 *   - 로그인 시 클라이언트가 보낸 id로 DB에서 사용자를 조회해야 한다
 *   - 조회한 사용자 정보로 비밀번호를 검증하고 JWT를 발급한다
 *
 * 어디서 사용되는가:
 *   - CustomUserDetailsService : findByUserId()로 사용자 조회 후 CustomUserDetails 생성
 *
 * JpaRepository<User, String> 상속:
 *   - User    : 이 Repository가 다루는 Entity 타입
 *   - String  : PK 타입 (userId가 String이므로)
 *   - 상속만으로 아래 메서드들이 자동 제공됨:
 *     save(user)        → INSERT / UPDATE
 *     findById(id)      → PK로 조회
 *     findAll()         → 전체 조회
 *     delete(user)      → DELETE
 *     count()           → 전체 개수
 *
 * findByUserId(String userId):
 *   - Spring Data JPA의 메서드명 규칙에 따라 자동으로 SQL이 생성된다
 *     "findBy" + "UserId" → WHERE user_id = ?
 *   - SQL을 직접 작성하지 않아도 된다
 *
 * Optional<User>:
 *   - 조회 결과가 없을 때 null 대신 Optional.empty()를 반환해서 NullPointerException을 방지한다
 *   - 호출하는 쪽에서 optional.isPresent() 또는 optional.orElseThrow()로 안전하게 처리 가능
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * user_id로 사용자를 조회한다
     * 실행되는 SQL: SELECT * FROM users WHERE user_id = ?
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자가 존재하면 Optional<User>, 없으면 Optional.empty()
     */
    Optional<User> findByUserId(String userId);
}
