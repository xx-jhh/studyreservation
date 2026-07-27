package com.example.studyreservation.room.dto;

import com.example.studyreservation.room.Room;

public record RoomResponse(Long id, String name, String description, int capacity) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getDescription(), room.getCapacity());
    }
}
