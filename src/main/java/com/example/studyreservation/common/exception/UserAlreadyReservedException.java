package com.example.studyreservation.common.exception;

public class UserAlreadyReservedException extends RuntimeException {

    public UserAlreadyReservedException() {
        super("같은 시간에 이미 다른 좌석을 예약하셨습니다.");
    }
}
