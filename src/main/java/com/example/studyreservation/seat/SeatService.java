package com.example.studyreservation.seat;

import com.example.studyreservation.common.exception.DuplicateSeatNumberException;
import com.example.studyreservation.common.exception.RoomNotFoundException;
import com.example.studyreservation.common.exception.SeatHasReservationException;
import com.example.studyreservation.common.exception.SeatNotFoundException;
import com.example.studyreservation.reservation.ReservationRepository;
import com.example.studyreservation.room.Room;
import com.example.studyreservation.room.RoomRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public Long registerSeat(Long roomId, String seatNumber) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        validateDuplicateSeatNumber(roomId, seatNumber);

        Seat seat = Seat.builder()
                .room(room)
                .seatNumber(seatNumber)
                .build();

        return seatRepository.save(seat).getId();
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        validateNoFutureReservation(seatId);

        seatRepository.delete(seat);
    }

    private void validateNoFutureReservation(Long seatId) {
        if (reservationRepository.existsBySeatIdAndReservationDateGreaterThanEqual(seatId, LocalDate.now(clock))) {
            throw new SeatHasReservationException();
        }
    }

    public List<Seat> findSeatsByRoom(Long roomId) {
        return seatRepository.findByRoomId(roomId);
    }

    public List<Seat> findAllSeats() {
        return seatRepository.findAll();
    }

    public Seat findSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(SeatNotFoundException::new);
    }

    @Transactional
    public void updateSeat(Long seatId, String seatNumber) {
        Seat seat = findSeatById(seatId);
        validateDuplicateSeatNumberForUpdate(seat.getRoom().getId(), seatNumber, seatId);
        seat.changeSeatNumber(seatNumber);
    }

    private void validateDuplicateSeatNumber(Long roomId, String seatNumber) {
        if (seatRepository.existsByRoomIdAndSeatNumber(roomId, seatNumber)) {
            throw new DuplicateSeatNumberException(seatNumber);
        }
    }

    private void validateDuplicateSeatNumberForUpdate(Long roomId, String seatNumber, Long seatId) {
        if (seatRepository.existsByRoomIdAndSeatNumberAndIdNot(roomId, seatNumber, seatId)) {
            throw new DuplicateSeatNumberException(seatNumber);
        }
    }
}
