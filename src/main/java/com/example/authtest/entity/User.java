package com.example.authtest.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * DB의 users 테이블과 1:1로 매핑되는 JPA 엔티티 클래스
 *
 * 역할:
 *   - JPA가 이 클래스를 보고 DB 테이블과 자동으로 매핑한다
 *   - UserRepository에서 DB를 조회하면 결과가 이 객체로 반환된다
 *
 * 왜 DTO(UserDto)와 분리하는가:
 *   - Entity는 DB 테이블 구조 그 자체를 표현하는 클래스다
 *   - Entity를 직접 외부(Controller, 클라이언트)에 노출하면:
 *     1. DB 구조가 외부에 노출됨
 *     2. JPA의 지연 로딩(Lazy Loading) 등 내부 동작이 외부로 새어나올 수 있음
 *     3. 필요 없는 필드까지 전달될 수 있음
 *   - 그래서 Entity → UserDto로 변환해서 사용하는 것이 일반적인 패턴이다
 *
 * 어디서 사용되는가:
 *   - UserRepository : findByUserId() 조회 결과가 Optional<User>로 반환됨
 *   - CustomUserDetailsService : User 엔티티를 꺼내서 UserDto로 변환 후 사용
 *
 * @Entity:
 *   - 이 클래스가 JPA 엔티티임을 선언. DB 테이블과 매핑됨
 *
 * @Table(name = "users"):
 *   - 매핑할 테이블명을 명시. 클래스명 User와 테이블명 users가 달라서 지정 필요
 *   - 생략하면 클래스명을 테이블명으로 사용 (User → user)
 *
 * @Id:
 *   - 해당 필드가 PK(Primary Key)임을 선언
 *
 * @Column(name = "컬럼명"):
 *   - 필드명과 DB 컬럼명이 다를 때 매핑 지정
 *   - userId(Java) ↔ user_id(DB), userType(Java) ↔ user_type(DB)
 *
 * @Getter (lombok):
 *   - @Data 대신 @Getter만 사용한 이유:
 *     Entity는 setter를 열어두면 JPA 영속성 컨텍스트 밖에서 값이 바뀌는 위험이 있다.
 *     읽기 전용으로 사용하기 위해 getter만 생성한다.
 */
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;      // 사용자 ID (PK)

    @Column(name = "passwd")
    private String passwd;      // 암호화된 비밀번호

    @Column(name = "email")
    private String email;       // 이메일

    @Column(name = "user_type")
    private String userType;    // 사용자 유형: U(일반), A(관리자)
}
