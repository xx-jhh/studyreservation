package com.example.studyreservation.seat.dto;

import com.example.studyreservation.seat.Seat;

public record SeatResponse(Long id, String seatNumber, Long roomId, String roomName) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getSeatNumber(), seat.getRoom().getId(), seat.getRoom().getName());
    }
}
