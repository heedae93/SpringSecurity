package com.example.authtest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HTML 페이지를 반환하는 Controller
 * @RestController가 아닌 @Controller를 사용해야 Thymeleaf 템플릿을 반환할 수 있다
 */
@Controller
public class PageController {

    /**
     * 로그인 테스트 페이지
     * GET http://localhost:9090/
     * templates/login.html 을 렌더링해서 반환
     */
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
}
