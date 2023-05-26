package com.gabrielbog.attendanceserver.models.responses;

import java.sql.Date;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class StudentAttendance {
    private String firstName;
    private String lastName;
    private Date date;
    private String state;
}
