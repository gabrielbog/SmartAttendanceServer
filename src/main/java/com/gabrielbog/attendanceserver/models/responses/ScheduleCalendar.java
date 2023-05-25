package com.gabrielbog.attendanceserver.models.responses;

import java.sql.Date;
import java.sql.Time;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class ScheduleCalendar {

    private Date date;
    private Time timeStart;
    private Time timeStop;
    private String grup;

    public int compareTo(ScheduleCalendar other) {
        //compare by date
        int dateCompare = this.date.compareTo(other.date);
        if (dateCompare != 0) {
            return dateCompare;
        }

        //compare by start time
        int timeStartCompare = this.timeStart.compareTo(other.timeStart);
        if (timeStartCompare != 0) {
            return timeStartCompare;
        }

        //compare by stop time
        int timeStopCompare = this.timeStop.compareTo(other.timeStop);
        if (timeStopCompare != 0) {
            return timeStopCompare;
        }

        //compare by grup
        return this.grup.compareTo(other.grup);
    }
}
