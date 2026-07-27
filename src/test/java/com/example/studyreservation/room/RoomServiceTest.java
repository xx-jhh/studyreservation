package com.example.studyreservation.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studyreservation.common.exception.RoomNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
    }

    @Test
    void 룸을_등록하면_입력한_정보_그대로_저장된다() {
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            ReflectionTestUtils.setField(room, "id", 1L);
            return room;
        });

        Long roomId = roomService.registerRoom("1층 스터디룸", "조용한 공간", 20);

        assertThat(roomId).isEqualTo(1L);

        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(captor.capture());
        Room saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("1층 스터디룸");
        assertThat(saved.getDescription()).isEqualTo("조용한 공간");
        assertThat(saved.getCapacity()).isEqualTo(20);
    }

    @Test
    void 전체_룸_목록을_조회한다() {
        Room room = Room.builder().name("1층 스터디룸").description("설명").capacity(20).build();
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> rooms = roomService.findAllRooms();

        assertThat(rooms).containsExactly(room);
    }

    @Test
    void 존재하는_룸을_id로_조회한다() {
        Room room = Room.builder().name("1층 스터디룸").description("설명").capacity(20).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThat(roomService.findRoomById(1L)).isEqualTo(room);
    }

    @Test
    void 존재하지_않는_룸을_조회하면_예외() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findRoomById(999L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void 룸_정보를_수정하면_반영된다() {
        Room room = Room.builder().name("1층 스터디룸").description("설명").capacity(20).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.updateRoom(1L, "2층 스터디룸", "새 설명", 30);

        assertThat(room.getName()).isEqualTo("2층 스터디룸");
        assertThat(room.getDescription()).isEqualTo("새 설명");
        assertThat(room.getCapacity()).isEqualTo(30);
    }

    @Test
    void 존재하지_않는_룸을_수정하려_하면_예외() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(999L, "이름", "설명", 10))
                .isInstanceOf(RoomNotFoundException.class);
    }
}
