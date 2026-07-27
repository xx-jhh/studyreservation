package com.example.studyreservation.common.exception;

public class DuplicateSeatNumberException extends RuntimeException {

    public DuplicateSeatNumberException(String seatNumber) {
        super("이미 등록된 좌석 번호입니다: " + seatNumber);
    }
}
