package com.example.studyreservation.common.exception;

public class DailyReservationLimitExceededException extends RuntimeException {

    public DailyReservationLimitExceededException(int maxDailyHours) {
        super("하루 최대 예약 가능 시간(" + maxDailyHours + "시간)을 초과했습니다.");
    }
}
