package com.example.studyreservation.room;

import com.example.studyreservation.common.exception.RoomNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public Long registerRoom(String name, String description, int capacity) {
        Room room = Room.builder()
                .name(name)
                .description(description)
                .capacity(capacity)
                .build();

        return roomRepository.save(room).getId();
    }

    public List<Room> findAllRooms() {
        return roomRepository.findAll();
    }

    public Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);
    }
}
