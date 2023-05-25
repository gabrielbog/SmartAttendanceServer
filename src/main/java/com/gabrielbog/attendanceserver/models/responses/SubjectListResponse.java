package com.gabrielbog.attendanceserver.models.responses;

import com.gabrielbog.attendanceserver.models.Subject;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SubjectListResponse {
    private int code;
    private List<Subject> subjectList;
}
