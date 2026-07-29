package com.example.studyreservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 전체_예약_조회는_user와_seat_room까지_함께_초기화된다() {
        List<Reservation> all = reservationRepository.findAll();
        assertThat(all).isNotEmpty();

        for (Reservation reservation : all) {
            assertThat(Hibernate.isInitialized(reservation.getUser())).isTrue();
            assertThat(Hibernate.isInitialized(reservation.getSeat())).isTrue();
            assertThat(Hibernate.isInitialized(reservation.getSeat().getRoom())).isTrue();
        }
    }

    @Test
    void 사용자별_예약_조회는_seat_room까지_함께_초기화된다() {
        Long userId = reservationRepository.findAll().get(0).getUser().getId();

        List<Reservation> myReservations = reservationRepository
                .findByUserIdOrderByReservationDateDescStartTimeDesc(userId);
        assertThat(myReservations).isNotEmpty();

        for (Reservation reservation : myReservations) {
            assertThat(Hibernate.isInitialized(reservation.getSeat())).isTrue();
            assertThat(Hibernate.isInitialized(reservation.getSeat().getRoom())).isTrue();
        }
    }
}
