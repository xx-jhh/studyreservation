package com.example.studyreservation.seat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void 전체_좌석_조회는_room까지_함께_초기화된다() {
        List<Seat> all = seatRepository.findAll();
        assertThat(all).isNotEmpty();

        for (Seat seat : all) {
            assertThat(Hibernate.isInitialized(seat.getRoom())).isTrue();
        }
    }
}
