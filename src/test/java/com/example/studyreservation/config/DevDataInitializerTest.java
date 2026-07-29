package com.example.studyreservation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studyreservation.reservation.ReservationRepository;
import com.example.studyreservation.reservation.ReservationService;
import com.example.studyreservation.room.Room;
import com.example.studyreservation.room.RoomRepository;
import com.example.studyreservation.seat.Seat;
import com.example.studyreservation.seat.SeatService;
import com.example.studyreservation.user.Role;
import com.example.studyreservation.user.User;
import com.example.studyreservation.user.UserRepository;
import com.example.studyreservation.user.UserService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DevDataInitializerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 7, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private SeatService seatService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationService reservationService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DevDataInitializer initializer;
    private Room room;
    private Seat seat1;
    private Seat seat2;

    @BeforeEach
    void setUp() {
        initializer = new DevDataInitializer(roomRepository, seatService, userRepository, userService,
                reservationRepository, reservationService, passwordEncoder, FIXED_CLOCK);

        room = Room.builder().name("1층 스터디룸").description("설명").capacity(4).build();
        ReflectionTestUtils.setField(room, "id", 1L);

        seat1 = Seat.builder().room(room).seatNumber("A1").build();
        ReflectionTestUtils.setField(seat1, "id", 10L);
        seat2 = Seat.builder().room(room).seatNumber("A2").build();
        ReflectionTestUtils.setField(seat2, "id", 11L);
    }

    @Test
    void 예약이_없으면_사용자2명과_예약3건을_생성한다() {
        when(reservationRepository.count()).thenReturn(0L);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(seatService.findSeatsByRoom(1L)).thenReturn(List.of(seat1, seat2));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin1234")).thenReturn("encoded-admin");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 100L);
            return user;
        });
        when(userService.signUp("user@example.com", "user1234", "테스트유저")).thenReturn(200L);

        initializer.run();

        ArgumentCaptor<User> adminCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(adminCaptor.capture());
        assertThat(adminCaptor.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(adminCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);

        LocalDate tomorrow = LocalDate.of(2026, 7, 2);
        LocalDate dayAfterTomorrow = LocalDate.of(2026, 7, 3);
        verify(reservationService).reserve(100L, 10L, tomorrow, LocalTime.of(10, 0), LocalTime.of(11, 0));
        verify(reservationService).reserve(200L, 11L, tomorrow, LocalTime.of(14, 0), LocalTime.of(15, 0));
        verify(reservationService).reserve(200L, 10L, dayAfterTomorrow, LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    @Test
    void 이미_예약이_있으면_아무것도_생성하지_않는다() {
        when(reservationRepository.count()).thenReturn(5L);

        initializer.run();

        verify(roomRepository, never()).findAll();
        verify(userRepository, never()).save(any());
        verify(userService, never()).signUp(any(), any(), any());
        verify(reservationService, never()).reserve(any(), any(), any(), any(), any());
    }

    @Test
    void 예약은_없지만_사용자가_이미_있으면_새로_만들지_않고_재사용한다() {
        when(reservationRepository.count()).thenReturn(0L);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(seatService.findSeatsByRoom(1L)).thenReturn(List.of(seat1, seat2));
        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(existingUser(100L)));
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(existingUser(200L)));

        initializer.run();

        verify(userRepository, never()).save(any());
        verify(userService, never()).signUp(any(), any(), any());
        verify(reservationService).reserve(eq(100L), eq(10L), any(), any(), any());
    }

    private User existingUser(Long id) {
        User user = User.builder().email("x@example.com").password("pw").name("이름").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
