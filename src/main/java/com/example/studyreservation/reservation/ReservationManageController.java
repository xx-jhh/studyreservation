package com.example.studyreservation.reservation;

import com.example.studyreservation.reservation.dto.ReservationRequest;
import com.example.studyreservation.reservation.dto.ReservationResponse;
import com.example.studyreservation.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationManageController {

    private final ReservationService reservationService;

    @GetMapping("/me")
    public String myReservations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Reservation> reservations = reservationService.findMyReservations(userDetails.getId());

        List<ReservationResponse> responses = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getReservationGroupId, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(ReservationResponse::from)
                .toList();

        model.addAttribute("reservations", responses);
        return "reservation/list";
    }

    @PostMapping("/{groupId}/cancel")
    public String cancel(@PathVariable String groupId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationService.cancelReservation(userDetails.getId(), groupId);
        return "redirect:/reservations/me";
    }

    @GetMapping("/{groupId}/edit")
    public String editForm(@PathVariable String groupId, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Reservation> reservations = reservationService.findReservationGroupForUser(userDetails.getId(), groupId);

        model.addAttribute("reservationRequest", toEditRequest(reservations));
        model.addAttribute("seat", reservations.get(0).getSeat());
        model.addAttribute("groupId", groupId);
        addTimeOptions(model);
        return "reservation/edit";
    }

    @PostMapping("/{groupId}/edit")
    public String edit(@PathVariable String groupId,
                        @Valid @ModelAttribute("reservationRequest") ReservationRequest request,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        if (bindingResult.hasErrors()) {
            List<Reservation> reservations = reservationService.findReservationGroupForUser(userDetails.getId(), groupId);
            model.addAttribute("seat", reservations.get(0).getSeat());
            model.addAttribute("groupId", groupId);
            addTimeOptions(model);
            return "reservation/edit";
        }

        reservationService.modifyReservation(
                userDetails.getId(),
                groupId,
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        return "redirect:/reservations/me";
    }

    private ReservationRequest toEditRequest(List<Reservation> reservations) {
        Reservation first = reservations.get(0);
        LocalTime earliestStart = reservations.stream()
                .map(Reservation::getStartTime)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        LocalTime latestEnd = reservations.stream()
                .map(reservation -> reservation.getStartTime().plusHours(1))
                .max(Comparator.naturalOrder())
                .orElseThrow();

        ReservationRequest request = new ReservationRequest();
        request.setSeatId(first.getSeat().getId());
        request.setReservationDate(first.getReservationDate());
        request.setStartTime(earliestStart);
        request.setEndTime(latestEnd);
        return request;
    }

    private void addTimeOptions(Model model) {
        model.addAttribute("startTimeOptions", ReservationService.startTimeOptions());
        model.addAttribute("endTimeOptions", ReservationService.endTimeOptions());
    }
}
