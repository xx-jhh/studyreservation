package com.example.studyreservation.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsBySeatIdAndReservationDateAndStartTime(Long seatId, LocalDate reservationDate, LocalTime startTime);

    boolean existsBySeatIdAndReservationDateGreaterThanEqual(Long seatId, LocalDate reservationDate);

    boolean existsByUserIdAndReservationDateAndStartTime(Long userId, LocalDate reservationDate, LocalTime startTime);

    @Override
    @EntityGraph(attributePaths = {"user", "seat", "seat.room"})
    List<Reservation> findAll();

    @EntityGraph(attributePaths = {"seat", "seat.room"})
    List<Reservation> findByUserIdOrderByReservationDateDescStartTimeDesc(Long userId);

    List<Reservation> findBySeat_Room_IdAndReservationDate(Long roomId, LocalDate reservationDate);

    List<Reservation> findByReservationGroupId(String reservationGroupId);

    long countByUserIdAndReservationDate(Long userId, LocalDate reservationDate);
}
