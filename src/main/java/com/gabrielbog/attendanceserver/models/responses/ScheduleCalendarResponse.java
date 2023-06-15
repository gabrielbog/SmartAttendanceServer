package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
public class ScheduleCalendarResponse {
    private int code;
    private List<ScheduleCalendar> scheduleCalendarList;
}
