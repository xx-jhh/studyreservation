package com.example.studyreservation.user;

import com.example.studyreservation.user.dto.SignUpRequest;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpRequest", new SignUpRequest());
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("signUpRequest") SignUpRequest request, BindingResult bindingResult) {
        if (!Objects.equals(request.getPassword(), request.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "passwordMismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        userService.signUp(request.getEmail(), request.getPassword(), request.getName());
        return "redirect:/login";
    }
}
