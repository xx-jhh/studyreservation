package com.example.studyreservation.room;

import com.example.studyreservation.room.dto.RoomRegisterRequest;
import com.example.studyreservation.room.dto.RoomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    @GetMapping
    public String roomList(Model model) {
        model.addAttribute("rooms", roomService.findAllRooms().stream().map(RoomResponse::from).toList());
        return "room/admin/list";
    }

    @GetMapping("/new")
    public String newRoomForm(Model model) {
        model.addAttribute("roomRegisterRequest", new RoomRegisterRequest());
        return "room/admin/new";
    }

    @PostMapping
    public String registerRoom(@Valid @ModelAttribute("roomRegisterRequest") RoomRegisterRequest request,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "room/admin/new";
        }

        roomService.registerRoom(request.getName(), request.getDescription(), request.getCapacity());
        return "redirect:/admin/rooms/new";
    }
}
