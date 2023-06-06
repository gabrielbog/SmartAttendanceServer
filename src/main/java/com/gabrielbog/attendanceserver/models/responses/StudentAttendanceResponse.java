package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class StudentAttendanceResponse {
    private int code;
    private int completeCalendarCount;
    private List<StudentAttendance> studentAttendanceList;
}
