package com.example.studyreservation.seat;

import com.example.studyreservation.common.exception.DuplicateSeatNumberException;
import com.example.studyreservation.common.exception.RoomNotFoundException;
import com.example.studyreservation.common.exception.SeatNotFoundException;
import com.example.studyreservation.room.Room;
import com.example.studyreservation.room.RoomRepository;
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

        seatRepository.delete(seat);
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

    private void validateDuplicateSeatNumber(Long roomId, String seatNumber) {
        if (seatRepository.existsByRoomIdAndSeatNumber(roomId, seatNumber)) {
            throw new DuplicateSeatNumberException(seatNumber);
        }
    }
}
