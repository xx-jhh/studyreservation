package com.example.studyreservation.common.exception;

public class ReservationAccessDeniedException extends RuntimeException {

    public ReservationAccessDeniedException() {
        super("본인의 예약만 처리할 수 있습니다.");
    }
}
