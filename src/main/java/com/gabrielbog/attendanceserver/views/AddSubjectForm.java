package com.gabrielbog.attendanceserver.views;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class AddSubjectForm {

    private String name;
    private String specialization;
    private String grade;
    private String type;
    private String semester;
    private String attendanceTotal;
    private String absencesAllowed;
}
