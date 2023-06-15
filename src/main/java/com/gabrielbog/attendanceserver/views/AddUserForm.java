package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Data
public class AddUserForm {
    private String firstName;
    private String lastName;
    private String cnp;
    private String password;
}
