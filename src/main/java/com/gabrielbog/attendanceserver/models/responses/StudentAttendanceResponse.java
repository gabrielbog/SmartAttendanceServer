package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
public class StudentAttendanceResponse {
    private int code;
    private int completeCalendarCount;
    private List<StudentAttendance> studentAttendanceList;
}
