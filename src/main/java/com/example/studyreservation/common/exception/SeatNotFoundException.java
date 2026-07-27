package com.example.studyreservation.common.exception;

public class SeatNotFoundException extends RuntimeException {

    public SeatNotFoundException() {
        super("존재하지 않는 좌석입니다.");
    }
}
