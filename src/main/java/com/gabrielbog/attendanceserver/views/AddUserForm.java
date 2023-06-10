package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class AddUserForm {
    private String firstName;
    private String lastName;
    private String cnp;
    private String password;
}
