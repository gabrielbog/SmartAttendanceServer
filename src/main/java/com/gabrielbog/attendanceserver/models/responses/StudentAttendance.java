package com.gabrielbog.attendanceserver.models.responses;

import java.sql.Date;
import java.sql.Time;

import lombok.*;

@AllArgsConstructor
@Data
public class StudentAttendance {
    private int type; //0 - for professors, toString with all data; 1 - for students, ignores the name fields
    private Date date;
    private Time timeStart; //these are used when listing total attendance for all students
    private Time timeStop;
    private String firstName; //change name to firstString - to repurpose into timeStart for students
    private String lastName; //change name to lastString - to repurpose into timeStop for students
    private String state;
}
