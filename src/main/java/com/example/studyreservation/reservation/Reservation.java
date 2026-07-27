package com.example.studyreservation.reservation;

import com.example.studyreservation.common.BaseTimeEntity;
import com.example.studyreservation.seat.Seat;
import com.example.studyreservation.user.User;
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
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"seat_id", "reservation_date", "start_time"}),
                @UniqueConstraint(columnNames = {"user_id", "reservation_date", "start_time"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "reservation_group_id", nullable = false, length = 36)
    private String reservationGroupId;

    @Builder
    public Reservation(User user, Seat seat, LocalDate reservationDate, LocalTime startTime, String reservationGroupId) {
        this.user = user;
        this.seat = seat;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.reservationGroupId = reservationGroupId;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}
