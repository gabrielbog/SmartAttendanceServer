package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Data
public class AddScheduleForm {
    private String subject;
    private String professor;
    private String timeStart;
    private String timeStop;
    private String weekday;
    private String grup;
    private String room;
}
