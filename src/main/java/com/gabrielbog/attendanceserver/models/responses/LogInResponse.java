package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

@AllArgsConstructor
@Data
public class LogInResponse {
    private int code;
    private int id;
    private int isAdmin;
    private String firstName;
    private String lastName;
}
