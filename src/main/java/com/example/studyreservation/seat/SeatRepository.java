package com.example.studyreservation.seat;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Override
    @EntityGraph(attributePaths = {"room"})
    List<Seat> findAll();

    List<Seat> findByRoomId(Long roomId);

    boolean existsByRoomIdAndSeatNumber(Long roomId, String seatNumber);

    boolean existsByRoomIdAndSeatNumberAndIdNot(Long roomId, String seatNumber, Long id);
}
