package com.example.studyreservation.reservation.dto;

import com.example.studyreservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record AdminReservationResponse(
        String reservationGroupId,
        String userName,
        String userEmail,
        String roomName,
        String seatNumber,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime
) {

    public static AdminReservationResponse from(List<Reservation> sameGroupReservations) {
        Reservation first = sameGroupReservations.get(0);

        LocalTime earliestStart = sameGroupReservations.stream()
                .map(Reservation::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        LocalTime latestEnd = sameGroupReservations.stream()
                .map(reservation -> reservation.getStartTime().plusHours(1))
                .max(Comparator.naturalOrder())
                .orElseThrow();

        return new AdminReservationResponse(
                first.getReservationGroupId(),
                first.getUser().getName(),
                first.getUser().getEmail(),
                first.getSeat().getRoom().getName(),
                first.getSeat().getSeatNumber(),
                first.getReservationDate(),
                earliestStart,
                latestEnd
        );
    }
}
