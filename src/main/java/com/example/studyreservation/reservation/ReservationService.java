package com.example.studyreservation.reservation;

import com.example.studyreservation.common.exception.DailyReservationLimitExceededException;
import com.example.studyreservation.common.exception.InvalidReservationTimeException;
import com.example.studyreservation.common.exception.ReservationAccessDeniedException;
import com.example.studyreservation.common.exception.ReservationNotFoundException;
import com.example.studyreservation.common.exception.SeatAlreadyReservedException;
import com.example.studyreservation.common.exception.SeatNotFoundException;
import com.example.studyreservation.common.exception.UserAlreadyReservedException;
import com.example.studyreservation.seat.Seat;
import com.example.studyreservation.seat.SeatRepository;
import com.example.studyreservation.user.User;
import com.example.studyreservation.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    public static final int OPENING_HOUR = 6;
    public static final int CLOSING_HOUR = 24;
    public static final int MAX_DAILY_HOURS_PER_USER = 8;

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public String reserve(Long userId, Long seatId, LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        validateTimeRange(startTime, endTime);

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(SeatNotFoundException::new);
        User user = userRepository.getReferenceById(userId);

        List<LocalTime> slots = splitIntoSlots(startTime, endTime);
        validateDailyLimit(userId, reservationDate, slots.size());
        validateNoDuplicate(userId, seatId, reservationDate, slots);

        String reservationGroupId = UUID.randomUUID().toString();
        for (LocalTime slot : slots) {
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .seat(seat)
                    .reservationDate(reservationDate)
                    .startTime(slot)
                    .reservationGroupId(reservationGroupId)
                    .build();
            reservationRepository.save(reservation);
        }

        return reservationGroupId;
    }

    @Transactional
    public void cancelReservation(Long userId, String reservationGroupId) {
        List<Reservation> reservations = findReservationGroupForUser(userId, reservationGroupId);
        reservationRepository.deleteAll(reservations);
    }

    @Transactional
    public String modifyReservation(Long userId, String reservationGroupId, LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime) {
        List<Reservation> existing = findReservationGroupForUser(userId, reservationGroupId);
        validateTimeRange(newStartTime, newEndTime);

        Seat seat = existing.get(0).getSeat();
        User user = existing.get(0).getUser();
        reservationRepository.deleteAll(existing);

        List<LocalTime> slots = splitIntoSlots(newStartTime, newEndTime);
        validateDailyLimit(userId, newDate, slots.size());
        validateNoDuplicate(userId, seat.getId(), newDate, slots);

        for (LocalTime slot : slots) {
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .seat(seat)
                    .reservationDate(newDate)
                    .startTime(slot)
                    .reservationGroupId(reservationGroupId)
                    .build();
            reservationRepository.save(reservation);
        }

        return reservationGroupId;
    }

    public List<Reservation> findReservationGroupForUser(Long userId, String reservationGroupId) {
        List<Reservation> reservations = reservationRepository.findByReservationGroupId(reservationGroupId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException();
        }

        if (!reservations.get(0).isOwnedBy(userId)) {
            throw new ReservationAccessDeniedException();
        }

        return reservations;
    }

    public List<Reservation> findMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByReservationDateDescStartTimeDesc(userId);
    }

    public List<Reservation> findReservedSlots(Long roomId, LocalDate reservationDate) {
        return reservationRepository.findBySeat_Room_IdAndReservationDate(roomId, reservationDate);
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.getMinute() != 0 || endTime.getMinute() != 0) {
            throw new InvalidReservationTimeException("예약 시간은 정시 단위(1시간 슬롯)로만 가능합니다.");
        }
        if (startTime.getHour() < OPENING_HOUR) {
            throw new InvalidReservationTimeException("영업 시작 시간(06:00) 이전은 예약할 수 없습니다.");
        }
        if (toExclusiveEndHour(endTime) <= startTime.getHour()) {
            throw new InvalidReservationTimeException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }
        if (toExclusiveEndHour(endTime) > CLOSING_HOUR) {
            throw new InvalidReservationTimeException("영업 종료 시간(자정) 이후는 예약할 수 없습니다.");
        }
    }

    private List<LocalTime> splitIntoSlots(LocalTime startTime, LocalTime endTime) {
        int endHour = toExclusiveEndHour(endTime);
        List<LocalTime> slots = new ArrayList<>();
        for (int hour = startTime.getHour(); hour < endHour; hour++) {
            slots.add(LocalTime.of(hour, 0));
        }
        return slots;
    }

    private int toExclusiveEndHour(LocalTime endTime) {
        return endTime.equals(LocalTime.MIDNIGHT) ? CLOSING_HOUR : endTime.getHour();
    }

    public static List<LocalTime> startTimeOptions() {
        return IntStream.range(OPENING_HOUR, CLOSING_HOUR)
                .mapToObj(hour -> LocalTime.of(hour, 0))
                .toList();
    }

    public static List<LocalTime> endTimeOptions() {
        return Stream.concat(
                        IntStream.range(OPENING_HOUR + 1, CLOSING_HOUR).mapToObj(hour -> LocalTime.of(hour, 0)),
                        Stream.of(LocalTime.MIDNIGHT))
                .toList();
    }

    private void validateDailyLimit(Long userId, LocalDate reservationDate, int newSlotCount) {
        long existingHours = reservationRepository.countByUserIdAndReservationDate(userId, reservationDate);
        if (existingHours + newSlotCount > MAX_DAILY_HOURS_PER_USER) {
            throw new DailyReservationLimitExceededException(MAX_DAILY_HOURS_PER_USER);
        }
    }

    private void validateNoDuplicate(Long userId, Long seatId, LocalDate reservationDate, List<LocalTime> slots) {
        for (LocalTime slot : slots) {
            if (reservationRepository.existsBySeatIdAndReservationDateAndStartTime(seatId, reservationDate, slot)) {
                throw new SeatAlreadyReservedException();
            }
            if (reservationRepository.existsByUserIdAndReservationDateAndStartTime(userId, reservationDate, slot)) {
                throw new UserAlreadyReservedException();
            }
        }
    }
}
