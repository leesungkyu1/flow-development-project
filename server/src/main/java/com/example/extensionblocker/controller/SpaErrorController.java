package com.example.extensionblocker.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Single Page Application (SPA)의 클라이언트 측 라우팅을 지원하기 위한 컨트롤러.
 * 서버에서 404 오류 발생 시, 모든 요청을 `index.html`로 포워딩하여 클라이언트 라우터가 처리하도록 합니다.
 */
@Controller
public class SpaErrorController implements ErrorController {

    private static final String PATH = "/error";

    /**
     * 오류 발생 시 `index.html`로 포워딩하여 SPA 클라이언트 라우터가 처리하도록 합니다.
     *
     * @return `index.html`로의 포워딩 경로
     */
    @RequestMapping(PATH)
    public String handleError() {
        return "forward:/index.html";
    }

}
