package com.example.studyreservation.common.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException() {
        super("존재하지 않는 예약입니다.");
    }
}
