package com.example.studyreservation.common.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException() {
        super("존재하지 않는 룸입니다.");
    }
}
