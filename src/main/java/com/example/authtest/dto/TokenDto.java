package com.example.authtest.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 로그인 성공 시 클라이언트에게 반환하는 JWT 토큰 정보 DTO
 *
 * 역할:
 *   - 인증 성공 후 JwtTokenProvider가 생성한 JWT 토큰과 부가 정보를 담아서 클라이언트에 전달
 *
 * 왜 필요한가:
 *   - 로그인 성공 후 클라이언트는 이 토큰을 저장해두고,
 *     이후 모든 API 요청의 Header에 담아서 보낸다
 *     예) Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 *   - 클라이언트가 토큰 외에 user_id, user_type 같은 기본 정보도 바로 사용할 수 있도록 같이 내려준다
 *
 * 응답 예시:
 *   {
 *     "grantType": "bearer",
 *     "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
 *     "accessTokenExpiresIn": 1750000000000,
 *     "user_id": "user01",
 *     "user_type": "U"
 *   }
 *
 * 어디서 사용되는가:
 *   - JwtTokenProvider : 토큰 생성 후 TokenDto.builder()로 객체를 만들어 반환
 *   - AuthServiceImpl : JwtTokenProvider로부터 받은 TokenDto를 Controller로 전달
 *   - AuthController : 최종적으로 클라이언트에게 ResponseEntity로 반환
 *
 * @Builder (lombok):
 *   - 빌더 패턴으로 객체 생성 가능
 *   - TokenDto.builder()
 *             .grantType("bearer")
 *             .accessToken("eyJ...")
 *             .build()
 *   - 필드가 많을 때 생성자보다 가독성이 좋고 순서에 의존하지 않아도 됨
 */
@Data
@Builder
public class TokenDto {

    private String grantType;           // 토큰 타입. 항상 "bearer" 고정
    private String accessToken;         // 실제 JWT 문자열
    private Long accessTokenExpiresIn;  // 토큰 만료 시각 (밀리초 timestamp)
    private String user_id;             // 로그인한 사용자 ID
    private String user_type;           // 로그인한 사용자 유형 (U, A)
}
