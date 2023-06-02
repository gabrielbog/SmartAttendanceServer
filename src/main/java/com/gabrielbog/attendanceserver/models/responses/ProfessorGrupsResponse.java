package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProfessorGrupsResponse {
    private int code;
    private List<ProfessorGrups> professorGrupsList;
}
