package com.example.studyreservation.common.exception;

public class SeatHasReservationException extends RuntimeException {

    public SeatHasReservationException() {
        super("예약이 있는 좌석은 삭제할 수 없습니다.");
    }
}
