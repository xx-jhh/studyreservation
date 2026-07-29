package com.example.studyreservation.seat;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studyreservation.config.SecurityConfig;
import com.example.studyreservation.room.RoomService;
import com.example.studyreservation.security.CustomAccessDeniedHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminSeatController.class)
@Import({SecurityConfig.class, CustomAccessDeniedHandler.class})
class AdminSeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;
    @MockitoBean
    private RoomService roomService;

    @Test
    void 로그인하지_않으면_로그인_화면으로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/admin/seats"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/seats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_좌석_목록을_조회할_수_있다() throws Exception {
        when(seatService.findAllSeats()).thenReturn(List.of());

        mockMvc.perform(get("/admin/seats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_좌석을_삭제하면_목록으로_리다이렉트된다() throws Exception {
        mockMvc.perform(post("/admin/seats/{id}/delete", 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seats"));
    }
}
