package com.gabrielbog.attendanceserver.models.responses;

import java.sql.Date;
import java.sql.Time;

import lombok.*;

@AllArgsConstructor
@Data
public class ScheduleCalendar {

    private int id; //schedule id
    private Date date;
    private Time timeStart;
    private Time timeStop;
    private int grup;
}
