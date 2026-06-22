package com.example.authtest.dto;

import lombok.Data;

/**
 * 클라이언트의 로그인 요청 데이터를 담는 DTO
 *
 * 역할:
 *   - 클라이언트가 POST /auth/login 으로 보내는 요청 Body를 Java 객체로 받는 그릇
 *
 * 왜 필요한가:
 *   - HTTP 요청의 JSON Body를 Java 객체로 자동 변환(역직렬화)하려면 그릇 역할의 클래스가 필요하다
 *   - Controller에서 @RequestBody LoginDto loginDto 로 선언하면
 *     Spring이 JSON → LoginDto 객체로 자동 변환해준다
 *
 * 요청 예시:
 *   POST /auth/login
 *   {
 *     "id": "user01",
 *     "passwd": "1234",
 *     "user_type": "U"
 *   }
 *
 * 어디서 사용되는가:
 *   - AuthController : @RequestBody로 요청을 받아서 AuthService로 전달
 *   - AuthServiceImpl : id, passwd로 UsernamePasswordAuthenticationToken 생성
 *   - CustomAuthenticationProvider : user_type으로 어떤 유형의 사용자인지 분기 처리
 */
@Data
public class LoginDto {

    private String id;          // 로그인 아이디
    private String passwd;      // 로그인 비밀번호 (평문, 검증 후 버려짐)
    private String user_type;   // 사용자 유형: U(일반사용자), A(관리자)
}
