package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Entity
@Table(name="Scancodes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Scancode { //these entries should be removed automatically by the server once a schedule expires

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int subjectId;

    @Column
    private String code; //sha1-hashed code of schedule, professor and creation timestamp
}
