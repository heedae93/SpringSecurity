package com.example.authtest.config.jwt;

import com.example.authtest.config.security.auth.CustomUserDetails;
import com.example.authtest.dto.TokenDto;
import com.example.authtest.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰을 생성 / 검증 / 파싱하는 클래스
 *
 * 역할:
 *   - 인증 성공 후 JWT 액세스 토큰을 생성해서 클라이언트에게 발급
 *   - 이후 API 요청 시 클라이언트가 보내온 JWT가 유효한지 검증
 *   - 유효한 JWT에서 사용자 정보를 꺼내서 SecurityContext에 저장
 *
 * JWT 구조:
 *   eyJhbGciOiJIUzUxMiJ9         ← Header  : 알고리즘 정보 (HS512)
 *   .eyJ1c2VyX2lkIjoidXNlcjAxIn0 ← Payload : 담긴 데이터 (user_id, user_type, 만료시간 등)
 *   .SflKxwRJSMeKKF2QT4fwpMeJf   ← Signature : 서명 (위변조 방지)
 *
 * 왜 필요한가:
 *   - JWT 기반 인증에서 토큰은 서버가 발급하고 클라이언트가 보관한다
 *   - 이후 모든 요청에 토큰을 담아서 보내면 서버는 DB 조회 없이 토큰만으로 사용자를 식별한다
 *   - 이 클래스가 토큰의 생성/검증/파싱을 전담한다
 *
 * 어디서 사용되는가:
 *   - AuthServiceImpl : generateToken()으로 로그인 성공 시 JWT 발급
 *   - JwtFilter : validateToken()으로 요청마다 JWT 유효성 검사
 *                 getAuthentication()으로 JWT에서 사용자 정보 복원
 *
 * @Component:
 *   - Spring Bean으로 등록. @Autowired 또는 생성자 주입으로 사용 가능
 *
 * @Value("${jwt.secret.key}"):
 *   - application.yml의 jwt.secret.key 값을 주입받는다
 *   - 이 키로 JWT를 서명하고 검증한다. 외부에 절대 노출되어선 안된다
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // 토큰 만료 시간: 30분 (밀리초)
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30;

    // JWT 서명에 사용할 비밀 키
    // HS512 알고리즘 사용 (HMAC-SHA512)
    private final SecretKey secretKey;

    /**
     * 생성자에서 application.yml의 jwt.secret.key를 읽어서 SecretKey 객체로 변환
     * Base64로 인코딩된 문자열을 디코딩해서 키를 생성한다
     *
     * @param secretKey application.yml의 jwt.secret.key 값
     */
    public JwtTokenProvider(@Value("${jwt.secret.key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 인증 성공 후 JWT 액세스 토큰을 생성한다
     *
     * Payload에 담기는 데이터:
     *   - user_id  : 사용자 ID
     *   - user_type: 사용자 유형 (U, A)
     *   - roles    : 권한 목록 (ROLE_USER, ROLE_ADMIN)
     *   - iat      : 토큰 발급 시각
     *   - exp      : 토큰 만료 시각 (발급 시각 + 30분)
     *
     * @param authentication 인증 성공 후 CustomAuthenticationProvider가 반환한 인증 객체
     * @return TokenDto (accessToken, 만료시각, user_id, user_type 포함)
     */
    public TokenDto generateToken(Authentication authentication) {

        // 인증 객체에서 CustomUserDetails 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 권한 목록을 콤마로 구분된 문자열로 변환
        // 예) "ROLE_USER" 또는 "ROLE_ADMIN"
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date accessTokenExpiresIn = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        // JWT 생성
        String accessToken = Jwts.builder()
                .claim("user_id", user.getUserId())       // 사용자 ID
                .claim("user_type", user.getUserType())   // 사용자 유형
                .claim("roles", authorities)              // 권한 목록
                .setIssuedAt(now)                         // 발급 시각
                .setExpiration(accessTokenExpiresIn)      // 만료 시각
                .signWith(secretKey, SignatureAlgorithm.HS512) // 서명 (HS512 알고리즘)
                .compact();

        return TokenDto.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .user_id(user.getUserId())
                .user_type(user.getUserType())
                .build();
    }

    /**
     * JWT에서 인증 정보를 복원한다
     * JwtFilter에서 토큰 검증 후 SecurityContext에 저장할 때 사용한다
     *
     * JWT Payload에서 user_id, roles를 꺼내서
     * UsernamePasswordAuthenticationToken(인증 완료 상태)을 만들어 반환한다
     *
     * @param accessToken 클라이언트가 보내온 JWT 문자열
     * @return Authentication 객체 (SecurityContext에 저장됨)
     */
    public Authentication getAuthentication(String accessToken) {

        // JWT 파싱해서 Payload(Claims) 꺼내기
        Claims claims = parseClaims(accessToken);

        String userId = claims.get("user_id", String.class);
        String roles = claims.get("roles", String.class);

        // 권한 목록 생성
        // "ROLE_USER" 문자열 → GrantedAuthority 컬렉션으로 변환
        var authorities = java.util.Arrays.stream(roles.split(","))
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // DB 조회 없이 토큰의 정보만으로 인증 객체 생성
        // principal 자리에 userId 문자열을 넣는다 (DB 조회 불필요)
        return new UsernamePasswordAuthenticationToken(userId, "", authorities);
    }

    /**
     * JWT 유효성 검증
     * JwtFilter에서 요청마다 호출해서 토큰이 유효한지 확인한다
     *
     * 검증 항목:
     *   - 서명이 올바른가 (우리 서버가 발급한 토큰인가)
     *   - 만료되지 않았는가
     *   - 형식이 올바른가
     *
     * @param token 검증할 JWT 문자열
     * @return true: 유효한 토큰 / false: 유효하지 않은 토큰
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있습니다.");
        }
        return false;
    }

    /**
     * JWT 문자열을 파싱해서 Payload(Claims)를 꺼낸다
     * Claims에는 우리가 generateToken()에서 넣은 user_id, user_type, roles 등이 담겨있다
     *
     * @param accessToken JWT 문자열
     * @return Claims (Payload에 담긴 데이터 Map)
     */
    private Claims parseClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}
