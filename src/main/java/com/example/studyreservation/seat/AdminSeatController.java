package com.example.studyreservation.seat;

import com.example.studyreservation.room.RoomService;
import com.example.studyreservation.seat.dto.SeatRegisterRequest;
import com.example.studyreservation.seat.dto.SeatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/admin/seats")
public class AdminSeatController {

    private final SeatService seatService;
    private final RoomService roomService;

    @GetMapping
    public String seatList(Model model) {
        model.addAttribute("seats", seatService.findAllSeats().stream().map(SeatResponse::from).toList());
        return "seat/admin/list";
    }

    @GetMapping("/new")
    public String newSeatForm(Model model) {
        model.addAttribute("seatRegisterRequest", new SeatRegisterRequest());
        model.addAttribute("rooms", roomService.findAllRooms());
        return "seat/admin/new";
    }

    @PostMapping
    public String registerSeat(@Valid @ModelAttribute("seatRegisterRequest") SeatRegisterRequest request,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rooms", roomService.findAllRooms());
            return "seat/admin/new";
        }

        seatService.registerSeat(request.getRoomId(), request.getSeatNumber());
        return "redirect:/admin/seats/new";
    }

    @PostMapping("/{seatId}/delete")
    public String deleteSeat(@PathVariable Long seatId) {
        seatService.deleteSeat(seatId);
        return "redirect:/admin/seats";
    }

    @GetMapping("/{seatId}/edit")
    public String editForm(@PathVariable Long seatId, Model model) {
        Seat seat = seatService.findSeatById(seatId);

        SeatRegisterRequest request = new SeatRegisterRequest();
        request.setRoomId(seat.getRoom().getId());
        request.setSeatNumber(seat.getSeatNumber());

        model.addAttribute("seatId", seatId);
        model.addAttribute("roomName", seat.getRoom().getName());
        model.addAttribute("seatRegisterRequest", request);
        return "seat/admin/edit";
    }

    @PostMapping("/{seatId}/edit")
    public String edit(@PathVariable Long seatId,
                        @Valid @ModelAttribute("seatRegisterRequest") SeatRegisterRequest request,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("seatId", seatId);
            model.addAttribute("roomName", seatService.findSeatById(seatId).getRoom().getName());
            return "seat/admin/edit";
        }

        seatService.updateSeat(seatId, request.getSeatNumber());
        return "redirect:/admin/seats";
    }
}
