package com.gabrielbog.attendanceserver.models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class LogInResponse {
    private int code;
    private int id;
    private int isAdmin;
    private String firstName;
    private String lastName;
}
