package com.example.studyreservation.reservation;

import com.example.studyreservation.reservation.dto.ReservationRequest;
import com.example.studyreservation.seat.Seat;
import com.example.studyreservation.seat.SeatService;
import com.example.studyreservation.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final SeatService seatService;

    @GetMapping("/new")
    public String newReservationForm(@RequestParam Long seatId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                      Model model) {
        Seat seat = seatService.findSeatById(seatId);

        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSeatId(seatId);
        reservationRequest.setReservationDate(date != null ? date : LocalDate.now());

        model.addAttribute("reservationRequest", reservationRequest);
        model.addAttribute("seat", seat);
        addTimeOptions(model);
        return "reservation/new";
    }

    @PostMapping
    public String reserve(@Valid @ModelAttribute("reservationRequest") ReservationRequest request,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("seat", seatService.findSeatById(request.getSeatId()));
            addTimeOptions(model);
            return "reservation/new";
        }

        reservationService.reserve(
                userDetails.getId(),
                request.getSeatId(),
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        Seat seat = seatService.findSeatById(request.getSeatId());
        return "redirect:/rooms/" + seat.getRoom().getId() + "/seats?date=" + request.getReservationDate();
    }

    private void addTimeOptions(Model model) {
        model.addAttribute("startTimeOptions", ReservationService.startTimeOptions());
        model.addAttribute("endTimeOptions", ReservationService.endTimeOptions());
    }
}
