package com.example.studyreservation.seat.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record SeatAvailabilityResponse(Long seatId, String seatNumber, List<String> reservedTimeRanges) {

    public static SeatAvailabilityResponse of(Long seatId, String seatNumber, List<LocalTime> sortedReservedStartTimes) {
        return new SeatAvailabilityResponse(seatId, seatNumber, mergeIntoRanges(sortedReservedStartTimes));
    }

    private static List<String> mergeIntoRanges(List<LocalTime> sortedStartTimes) {
        List<String> ranges = new ArrayList<>();
        int i = 0;
        while (i < sortedStartTimes.size()) {
            LocalTime rangeStart = sortedStartTimes.get(i);
            LocalTime rangeEnd = rangeStart.plusHours(1);

            int j = i + 1;
            while (j < sortedStartTimes.size() && sortedStartTimes.get(j).equals(rangeEnd)) {
                rangeEnd = rangeEnd.plusHours(1);
                j++;
            }

            ranges.add(rangeStart + "~" + formatEnd(rangeEnd));
            i = j;
        }
        return ranges;
    }

    private static String formatEnd(LocalTime end) {
        return end.equals(LocalTime.MIDNIGHT) ? "24:00" : end.toString();
    }
}
