package com.example.studyreservation.reservation.dto;

import com.example.studyreservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationWithOwnerResponse(
        String reservationGroupId,
        String userName,
        String userEmail,
        String roomName,
        String seatNumber,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime
) {

    public static ReservationWithOwnerResponse from(List<Reservation> sameGroupReservations) {
        ReservationResponse base = ReservationResponse.from(sameGroupReservations);
        Reservation first = sameGroupReservations.get(0);

        return new ReservationWithOwnerResponse(
                base.reservationGroupId(),
                first.getUser().getName(),
                first.getUser().getEmail(),
                base.roomName(),
                base.seatNumber(),
                base.reservationDate(),
                base.startTime(),
                base.endTime()
        );
    }
}
