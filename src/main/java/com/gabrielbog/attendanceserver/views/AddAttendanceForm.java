package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Data
public class AddAttendanceForm {
    private String cnp;
    private String subject;
    private String date;
    private String time;
}
