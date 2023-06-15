package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
public class ProfessorGrupsResponse {
    private int code;
    private List<ProfessorGrups> professorGrupsList;
}
