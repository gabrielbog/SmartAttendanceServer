package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class AddStudentForm {
    private String firstName;
    private String lastName;
    private String cnp;
    private String password;
    private String specialization;
    private String grade;
    private String grup;
}
