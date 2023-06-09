package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

@AllArgsConstructor
@Data
public class ProfessorGrups {
    int grup; //this class exists for list clarity when using Postman

    public int compareTo(ProfessorGrups other) {
        return Integer.compare(this.grup, other.grup);
    }
}
