package com.gabrielbog.attendanceserver.models.responses;

import com.gabrielbog.attendanceserver.models.Subject;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
public class SubjectListResponse {
    private int code;
    private List<Subject> subjectList;
}
