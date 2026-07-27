package com.example.studyreservation.common.exception;

public class SeatAlreadyReservedException extends RuntimeException {

    public SeatAlreadyReservedException() {
        super("이미 예약된 시간대가 포함되어 있습니다. 다른 시간을 선택해 주세요.");
    }
}
