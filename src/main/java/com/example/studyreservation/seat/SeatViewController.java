package com.example.studyreservation.seat;

import com.example.studyreservation.reservation.Reservation;
import com.example.studyreservation.reservation.ReservationService;
import com.example.studyreservation.room.RoomService;
import com.example.studyreservation.seat.dto.SeatAvailabilityResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SeatViewController {

    private final RoomService roomService;
    private final SeatService seatService;
    private final ReservationService reservationService;

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAllRooms());
        return "seat/rooms";
    }

    @GetMapping("/rooms/{roomId}/seats")
    public String seats(@PathVariable Long roomId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         Model model) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        List<Seat> seats = seatService.findSeatsByRoom(roomId);
        List<Reservation> reservations = reservationService.findReservedSlots(roomId, targetDate);

        Map<Long, List<LocalTime>> reservedSlotsBySeat = reservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getSeat().getId(),
                        Collectors.mapping(Reservation::getStartTime, Collectors.toList())
                ));

        List<SeatAvailabilityResponse> seatAvailabilities = seats.stream()
                .map(seat -> SeatAvailabilityResponse.of(
                        seat.getId(),
                        seat.getSeatNumber(),
                        reservedSlotsBySeat.getOrDefault(seat.getId(), List.of()).stream()
                                .sorted(Comparator.naturalOrder())
                                .toList()
                ))
                .toList();

        model.addAttribute("room", roomService.findRoomById(roomId));
        model.addAttribute("date", targetDate);
        model.addAttribute("seats", seatAvailabilities);
        return "seat/seats";
    }
}
