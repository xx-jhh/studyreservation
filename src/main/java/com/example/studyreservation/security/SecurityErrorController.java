package com.example.studyreservation.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SecurityErrorController {

    @RequestMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("message", "접근 권한이 없습니다.");
        return "error";
    }
}
