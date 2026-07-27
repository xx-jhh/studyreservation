package com.example.studyreservation.common.exception;

public class InvalidReservationTimeException extends RuntimeException {

    public InvalidReservationTimeException(String message) {
        super(message);
    }
}
