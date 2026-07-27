package com.example.studyreservation.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoomRegisterRequest {

    @NotBlank(message = "룸 이름은 필수입니다.")
    @Size(max = 50, message = "룸 이름은 50자 이하여야 합니다.")
    private String name;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    private String description;

    @Positive(message = "수용 인원은 1명 이상이어야 합니다.")
    private int capacity;
}
