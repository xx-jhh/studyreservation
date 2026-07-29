package com.example.studyreservation.common.exception;

public class InvalidReservationTimeException extends RuntimeException {

    public enum Reason {
        NOT_HOUR_ALIGNED,
        BEFORE_OPENING,
        END_BEFORE_START,
        AFTER_CLOSING,
        PAST_TIME
    }

    public InvalidReservationTimeException(Reason reason) {
        super(messageFor(reason));
    }

    private static String messageFor(Reason reason) {
        return switch (reason) {
            case NOT_HOUR_ALIGNED -> "예약 시간은 정시 단위(1시간 슬롯)로만 가능합니다.";
            case BEFORE_OPENING -> "영업 시작 시간(06:00) 이전은 예약할 수 없습니다.";
            case END_BEFORE_START -> "종료 시간은 시작 시간보다 늦어야 합니다.";
            case AFTER_CLOSING -> "영업 종료 시간(자정) 이후는 예약할 수 없습니다.";
            case PAST_TIME -> "이미 지난 시간은 예약할 수 없습니다.";
        };
    }
}
