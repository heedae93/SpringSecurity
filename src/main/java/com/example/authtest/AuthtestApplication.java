package com.example.authtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션 진입점
 *
 * 역할:
 *   - 애플리케이션을 시작하는 main 메서드를 포함하는 클래스
 *
 * @SpringBootApplication 이 어노테이션 하나가 아래 세 가지를 한번에 처리한다:
 *   - @Configuration     : 이 클래스가 Spring Bean 설정 클래스임을 선언
 *   - @EnableAutoConfiguration : Spring Boot가 classpath의 의존성을 보고 자동으로 설정을 구성
 *                                예) spring-boot-starter-security 가 있으면 자동으로 Spring Security 활성화
 *                                    spring-boot-starter-data-jpa 가 있으면 자동으로 JPA 설정
 *   - @ComponentScan     : 현재 패키지(com.example.authtest) 하위의 모든 @Component, @Service,
 *                          @Repository, @Controller 를 자동으로 스캔해서 Spring Bean으로 등록
 *
 * SpringApplication.run() 이 하는 일:
 *   - Tomcat(내장 웹 서버)을 시작
 *   - Spring IoC 컨테이너(ApplicationContext) 생성
 *   - Bean 스캔 및 의존성 주입 처리
 *   - FilterChainProxy(Spring Security) 등록
 *   - 지정된 포트(application.yml의 server.port)로 HTTP 요청 대기
 */
@SpringBootApplication
public class AuthtestApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthtestApplication.class, args);
    }
}
