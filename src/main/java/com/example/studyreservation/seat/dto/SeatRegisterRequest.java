package com.example.studyreservation.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatRegisterRequest {

    @NotNull(message = "룸을 선택해야 합니다.")
    private Long roomId;

    @NotBlank(message = "좌석 번호는 필수입니다.")
    private String seatNumber;
}
