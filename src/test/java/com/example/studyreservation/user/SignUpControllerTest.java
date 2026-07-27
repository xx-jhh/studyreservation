package com.example.studyreservation.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.studyreservation.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SignUpController.class)
@Import(SecurityConfig.class)
class SignUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void 회원가입_폼을_보여준다() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"))
                .andExpect(model().attributeExists("signUpRequest"));
    }

    @Test
    void 이메일_형식이_잘못되면_다시_폼을_보여준다() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("email", "not-an-email")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("name", "테스터"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"))
                .andExpect(model().attributeHasFieldErrors("signUpRequest", "email"));

        verify(userService, never()).signUp(anyString(), anyString(), anyString());
    }

    @Test
    void 비밀번호가_8자_미만이면_다시_폼을_보여준다() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "1234")
                        .param("passwordConfirm", "1234")
                        .param("name", "테스터"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("signUpRequest", "password"));

        verify(userService, never()).signUp(anyString(), anyString(), anyString());
    }

    @Test
    void 비밀번호와_비밀번호확인이_다르면_다시_폼을_보여준다() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password123")
                        .param("passwordConfirm", "different123")
                        .param("name", "테스터"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("signUpRequest", "passwordConfirm"));

        verify(userService, never()).signUp(anyString(), anyString(), anyString());
    }

    @Test
    void 유효한_정보면_회원가입_처리_후_로그인_화면으로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/signup").with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("name", "테스터"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).signUp("user@example.com", "password123", "테스터");
    }
}
