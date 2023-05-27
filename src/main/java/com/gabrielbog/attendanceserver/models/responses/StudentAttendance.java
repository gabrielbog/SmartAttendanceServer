package com.gabrielbog.attendanceserver.models.responses;

import java.sql.Date;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentAttendance {
    private int type; //0 - for professors, toString with all data; 1 - for students, ignores the name fields
    private Date date;
    private String firstName; //change name to firstString - to repurpose into timeStart for students
    private String lastName; //change name to lastString - to repurpose into timeStop for students
    private String state;

    @Override
    public String toString() {
        if(type == 0) {
            return "StudentAttendance{" +
                    "date=" + date +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
        else {
            return "StudentAttendance{" +
                    "date=" + date +
                    ", state='" + state + '\'' +
                    '}';
        }
    }
}
