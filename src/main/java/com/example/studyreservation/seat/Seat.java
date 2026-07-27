package com.example.studyreservation.seat;

import com.example.studyreservation.common.BaseTimeEntity;
import com.example.studyreservation.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "seat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "seat_number"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Builder
    public Seat(Room room, String seatNumber) {
        this.room = room;
        this.seatNumber = seatNumber;
    }

    public void changeSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
