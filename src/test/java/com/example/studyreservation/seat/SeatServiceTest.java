package com.example.studyreservation.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studyreservation.common.exception.DuplicateSeatNumberException;
import com.example.studyreservation.common.exception.RoomNotFoundException;
import com.example.studyreservation.common.exception.SeatHasReservationException;
import com.example.studyreservation.common.exception.SeatNotFoundException;
import com.example.studyreservation.reservation.ReservationRepository;
import com.example.studyreservation.room.Room;
import com.example.studyreservation.room.RoomRepository;
import java.time.LocalDate;
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
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ReservationRepository reservationRepository;

    private SeatService seatService;

    private Room room;

    @BeforeEach
    void setUp() {
        seatService = new SeatService(seatRepository, roomRepository, reservationRepository);
        room = Room.builder().name("1층 스터디룸").description("설명").capacity(20).build();
        ReflectionTestUtils.setField(room, "id", 1L);
    }

    @Test
    void 좌석을_등록하면_해당_룸에_저장된다() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(seatRepository.existsByRoomIdAndSeatNumber(1L, "A1")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            ReflectionTestUtils.setField(seat, "id", 1L);
            return seat;
        });

        Long seatId = seatService.registerSeat(1L, "A1");

        assertThat(seatId).isEqualTo(1L);

        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository).save(captor.capture());
        assertThat(captor.getValue().getSeatNumber()).isEqualTo("A1");
        assertThat(captor.getValue().getRoom()).isEqualTo(room);
    }

    @Test
    void 존재하지_않는_룸에_좌석을_등록하면_예외() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.registerSeat(999L, "A1"))
                .isInstanceOf(RoomNotFoundException.class);

        verify(seatRepository, never()).save(any());
    }

    @Test
    void 같은_룸에_이미_있는_좌석번호로_등록하면_예외() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(seatRepository.existsByRoomIdAndSeatNumber(1L, "A1")).thenReturn(true);

        assertThatThrownBy(() -> seatService.registerSeat(1L, "A1"))
                .isInstanceOf(DuplicateSeatNumberException.class);

        verify(seatRepository, never()).save(any());
    }

    @Test
    void 존재하는_좌석을_삭제한다() {
        Seat seat = Seat.builder().room(room).seatNumber("A1").build();
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(reservationRepository.existsBySeatIdAndReservationDateGreaterThanEqual(1L, LocalDate.now()))
                .thenReturn(false);

        seatService.deleteSeat(1L);

        verify(seatRepository).delete(seat);
    }

    @Test
    void 오늘_이후_예약이_있는_좌석은_삭제할_수_없다() {
        Seat seat = Seat.builder().room(room).seatNumber("A1").build();
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(reservationRepository.existsBySeatIdAndReservationDateGreaterThanEqual(1L, LocalDate.now()))
                .thenReturn(true);

        assertThatThrownBy(() -> seatService.deleteSeat(1L))
                .isInstanceOf(SeatHasReservationException.class);

        verify(seatRepository, never()).delete(any());
    }

    @Test
    void 존재하지_않는_좌석을_삭제하려_하면_예외() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.deleteSeat(999L))
                .isInstanceOf(SeatNotFoundException.class);

        verify(seatRepository, never()).delete(any());
    }

    @Test
    void 존재하지_않는_좌석을_id로_조회하면_예외() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.findSeatById(999L))
                .isInstanceOf(SeatNotFoundException.class);
    }

    @Test
    void 룸별_좌석_목록을_조회한다() {
        Seat seat = Seat.builder().room(room).seatNumber("A1").build();
        when(seatRepository.findByRoomId(1L)).thenReturn(List.of(seat));

        assertThat(seatService.findSeatsByRoom(1L)).containsExactly(seat);
    }

    @Test
    void 좌석_번호를_수정하면_반영된다() {
        Seat seat = Seat.builder().room(room).seatNumber("A1").build();
        ReflectionTestUtils.setField(seat, "id", 10L);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatRepository.existsByRoomIdAndSeatNumberAndIdNot(1L, "B1", 10L)).thenReturn(false);

        seatService.updateSeat(10L, "B1");

        assertThat(seat.getSeatNumber()).isEqualTo("B1");
    }

    @Test
    void 다른_좌석이_이미_쓰는_번호로_수정하려_하면_예외() {
        Seat seat = Seat.builder().room(room).seatNumber("A1").build();
        ReflectionTestUtils.setField(seat, "id", 10L);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatRepository.existsByRoomIdAndSeatNumberAndIdNot(1L, "B1", 10L)).thenReturn(true);

        assertThatThrownBy(() -> seatService.updateSeat(10L, "B1"))
                .isInstanceOf(DuplicateSeatNumberException.class);

        assertThat(seat.getSeatNumber()).isEqualTo("A1");
    }

    @Test
    void 존재하지_않는_좌석을_수정하려_하면_예외() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.updateSeat(999L, "B1"))
                .isInstanceOf(SeatNotFoundException.class);
    }
}
