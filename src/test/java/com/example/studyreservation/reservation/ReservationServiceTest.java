package com.example.studyreservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studyreservation.common.exception.DailyReservationLimitExceededException;
import com.example.studyreservation.common.exception.InvalidReservationTimeException;
import com.example.studyreservation.common.exception.ReservationAccessDeniedException;
import com.example.studyreservation.common.exception.ReservationNotFoundException;
import com.example.studyreservation.common.exception.SeatAlreadyReservedException;
import com.example.studyreservation.common.exception.SeatNotFoundException;
import com.example.studyreservation.common.exception.UserAlreadyReservedException;
import com.example.studyreservation.room.Room;
import com.example.studyreservation.seat.Seat;
import com.example.studyreservation.seat.SeatRepository;
import com.example.studyreservation.user.User;
import com.example.studyreservation.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
class ReservationServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 1);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private UserRepository userRepository;

    private ReservationService reservationService;

    private Seat seat;
    private User user;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, seatRepository, userRepository);

        Room room = Room.builder().name("1층 스터디룸").description("설명").capacity(10).build();
        seat = Seat.builder().room(room).seatNumber("A1").build();
        setId(seat, 1L);
        user = User.builder().email("user@example.com").password("encoded").name("테스터").build();
        setId(user, 1L);
    }

    @Test
    void 여러_슬롯을_예약하면_같은_groupId로_저장된다() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        String groupId = reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 0), LocalTime.of(11, 0));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(2)).save(captor.capture());

        List<Reservation> saved = captor.getAllValues();
        assertThat(saved).extracting(Reservation::getStartTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThat(saved).allMatch(r -> r.getReservationGroupId().equals(groupId));
    }

    @Test
    void 자정까지_이어지는_예약도_정상적으로_슬롯이_나뉜다() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        reservationService.reserve(1L, 1L, DATE, LocalTime.of(22, 0), LocalTime.MIDNIGHT);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Reservation::getStartTime)
                .containsExactly(LocalTime.of(22, 0), LocalTime.of(23, 0));
    }

    @Test
    void 정시가_아니면_예외() {
        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 30), LocalTime.of(10, 0)))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @Test
    void 영업시작_이전이면_예외() {
        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(5, 0), LocalTime.of(6, 0)))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @Test
    void 종료시간이_시작시간보다_빠르거나_같으면_예외() {
        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(10, 0), LocalTime.of(9, 0)))
                .isInstanceOf(InvalidReservationTimeException.class);

        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(10, 0), LocalTime.of(10, 0)))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @Test
    void 존재하지_않는_좌석이면_예외() {
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(SeatNotFoundException.class);
    }

    @Test
    void 같은_좌석_같은_시간대에_이미_예약이_있으면_예외() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(reservationRepository.existsBySeatIdAndReservationDateAndStartTime(1L, DATE, LocalTime.of(9, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(SeatAlreadyReservedException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void 같은_사용자가_다른_좌석에서_같은_시간대를_이미_예약했으면_예외() {
        when(seatRepository.findById(2L)).thenReturn(Optional.of(seat));
        when(reservationRepository.existsBySeatIdAndReservationDateAndStartTime(2L, DATE, LocalTime.of(9, 0)))
                .thenReturn(false);
        when(reservationRepository.existsByUserIdAndReservationDateAndStartTime(1L, DATE, LocalTime.of(9, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(1L, 2L, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(UserAlreadyReservedException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void 그룹아이디에_해당하는_예약이_없으면_취소시_예외() {
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.cancelReservation(1L, "group-1"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void 본인_예약이_아니면_취소시_예외() {
        User owner = User.builder().email("owner@example.com").password("x").name("주인").build();
        setId(owner, 100L);
        Reservation reservation = Reservation.builder()
                .user(owner).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();

        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(999L, "group-1"))
                .isInstanceOf(ReservationAccessDeniedException.class);

        verify(reservationRepository, never()).deleteAll(any());
    }

    @Test
    void 본인_예약이면_그룹_전체가_삭제된다() {
        Reservation r1 = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        Reservation r2 = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(10, 0)).reservationGroupId("group-1").build();
        List<Reservation> group = List.of(r1, r2);

        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(group);

        reservationService.cancelReservation(1L, "group-1");

        verify(reservationRepository).deleteAll(group);
    }

    @Test
    void 예약을_수정하면_기존_슬롯을_지우고_새_슬롯을_같은_groupId로_저장한다() {
        Reservation existing = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of(existing));

        LocalDate newDate = LocalDate.of(2026, 8, 2);
        String groupId = reservationService.modifyReservation(1L, "group-1", newDate, LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertThat(groupId).isEqualTo("group-1");
        verify(reservationRepository).deleteAll(List.of(existing));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Reservation::getStartTime)
                .containsExactly(LocalTime.of(14, 0), LocalTime.of(15, 0));
        assertThat(captor.getAllValues()).allMatch(r -> r.getReservationGroupId().equals("group-1"));
        assertThat(captor.getAllValues()).allMatch(r -> r.getReservationDate().equals(newDate));
    }

    @Test
    void 존재하지_않는_예약을_수정하려_하면_예외() {
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.modifyReservation(1L, "group-1", DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(ReservationNotFoundException.class);

        verify(reservationRepository, never()).deleteAll(any());
    }

    @Test
    void 본인_예약이_아니면_수정시_예외() {
        User owner = User.builder().email("owner@example.com").password("x").name("주인").build();
        setId(owner, 100L);
        Reservation reservation = Reservation.builder().user(owner).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of(reservation));

        assertThatThrownBy(() -> reservationService.modifyReservation(999L, "group-1", DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(ReservationAccessDeniedException.class);

        verify(reservationRepository, never()).deleteAll(any());
    }

    @Test
    void 수정할_새_시간이_경계값을_어기면_기존_예약은_삭제되지_않는다() {
        Reservation existing = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.modifyReservation(1L, "group-1", DATE, LocalTime.of(5, 0), LocalTime.of(6, 0)))
                .isInstanceOf(InvalidReservationTimeException.class);

        verify(reservationRepository, never()).deleteAll(any());
    }

    @Test
    void 수정하려는_새_시간이_이미_예약되어_있으면_예외() {
        Reservation existing = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(List.of(existing));
        when(reservationRepository.existsBySeatIdAndReservationDateAndStartTime(1L, DATE, LocalTime.of(14, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.modifyReservation(1L, "group-1", DATE, LocalTime.of(14, 0), LocalTime.of(15, 0)))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    @Test
    void 하루_최대_예약_시간을_초과하면_예외() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(reservationRepository.countByUserIdAndReservationDate(1L, DATE)).thenReturn(6L);

        assertThatThrownBy(() -> reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 0), LocalTime.of(12, 0)))
                .isInstanceOf(DailyReservationLimitExceededException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void 하루_최대_예약_시간_경계값은_허용된다() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(reservationRepository.countByUserIdAndReservationDate(1L, DATE)).thenReturn(5L);

        reservationService.reserve(1L, 1L, DATE, LocalTime.of(9, 0), LocalTime.of(12, 0));

        verify(reservationRepository, times(3)).save(any());
    }

    @Test
    void 예약_수정시_기존_그룹의_시간은_한도_계산에서_제외된다() {
        Reservation existing1 = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(9, 0)).reservationGroupId("group-1").build();
        Reservation existing2 = Reservation.builder().user(user).seat(seat).reservationDate(DATE)
                .startTime(LocalTime.of(10, 0)).reservationGroupId("group-1").build();
        List<Reservation> existingGroup = List.of(existing1, existing2);
        when(reservationRepository.findByReservationGroupId("group-1")).thenReturn(existingGroup);

        // 삭제 전에는 이 그룹의 2시간을 포함해 하루 8시간을 이미 다 채운 상태
        when(reservationRepository.countByUserIdAndReservationDate(1L, DATE)).thenReturn(8L);
        // 그룹이 삭제되면(deleteAll 호출 시점) 남은 시간이 6시간으로 줄어든 것으로 시뮬레이션
        doAnswer(invocation -> {
            when(reservationRepository.countByUserIdAndReservationDate(1L, DATE)).thenReturn(6L);
            return null;
        }).when(reservationRepository).deleteAll(existingGroup);

        // 6시간 남은 상태에서 2시간짜리로 수정하면 총 8시간이라 한도 이내 -> 성공해야 함
        reservationService.modifyReservation(1L, "group-1", DATE, LocalTime.of(14, 0), LocalTime.of(16, 0));

        verify(reservationRepository, times(2)).save(any());
    }

    private void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
