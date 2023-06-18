package com.gabrielbog.attendanceserver.models;

import com.gabrielbog.attendanceserver.models.responses.ProfessorGrups;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name="Users")
@NoArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(columnDefinition = "integer default 0")
    private int isAdmin;

    @Column(unique = true)
    private String cnp;

    @Column
    private String password; //encrypted

    @Column
    private String firstName;

    @Column
    private String lastName;

    public int compareByName(User other) {
        int lastNamDiff = this.lastName.compareTo(other.lastName);
        if (lastNamDiff != 0) {
            return lastNamDiff;
        }
        return this.firstName.compareTo(other.firstName);
    }

}