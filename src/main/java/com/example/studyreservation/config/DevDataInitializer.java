package com.example.studyreservation.config;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final SeatService seatService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    public void run(String... args) {
        if (reservationRepository.count() > 0) {
            return;
        }

        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            return;
        }
        Room room = rooms.get(0);

        List<Seat> seats = seatService.findSeatsByRoom(room.getId());
        if (seats.isEmpty()) {
            return;
        }

        Long adminId = ensureUser("admin@example.com", "admin1234", "관리자", Role.ADMIN);
        Long userId = ensureUser("user@example.com", "user1234", "테스트유저", Role.USER);

        Long seat1 = seats.get(0).getId();
        Long seat2 = seats.get(seats.size() > 1 ? 1 : 0).getId();

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

        reservationService.reserve(adminId, seat1, tomorrow, LocalTime.of(10, 0), LocalTime.of(11, 0));
        reservationService.reserve(userId, seat2, tomorrow, LocalTime.of(14, 0), LocalTime.of(15, 0));
        reservationService.reserve(userId, seat1, dayAfterTomorrow, LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    private Long ensureUser(String email, String rawPassword, String name, Role role) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseGet(() -> createUser(email, rawPassword, name, role));
    }

    private Long createUser(String email, String rawPassword, String name, Role role) {
        if (role == Role.ADMIN) {
            return userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .name(name)
                    .role(role)
                    .build()).getId();
        }
        return userService.signUp(email, rawPassword, name);
    }
}
