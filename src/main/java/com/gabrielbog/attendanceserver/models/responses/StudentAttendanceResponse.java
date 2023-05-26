package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class StudentAttendanceResponse {
    private int code;
    private List<StudentAttendance> studentAttendanceList;
}
