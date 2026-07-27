package com.example.studyreservation.reservation;

import com.example.studyreservation.reservation.dto.AdminReservationResponse;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String allReservations(Model model) {
        List<Reservation> reservations = reservationService.findAllReservations();

        List<AdminReservationResponse> responses = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getReservationGroupId, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(AdminReservationResponse::from)
                .sorted(Comparator.comparing(AdminReservationResponse::reservationDate).reversed()
                        .thenComparing(AdminReservationResponse::startTime))
                .toList();

        model.addAttribute("reservations", responses);
        return "reservation/admin/list";
    }
}
