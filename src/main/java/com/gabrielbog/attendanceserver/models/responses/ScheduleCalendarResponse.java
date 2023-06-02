package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ScheduleCalendarResponse {
    private int code;
    private List<ScheduleCalendar> scheduleCalendarList;
}
