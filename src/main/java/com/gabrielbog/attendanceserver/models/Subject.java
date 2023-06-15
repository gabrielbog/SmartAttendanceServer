package com.gabrielbog.attendanceserver.models;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name="Subjects")
@NoArgsConstructor
@Data
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String name;

    @Column
    private int spec; //specialization

    @Column
    private int grade; //year

    @Column
    private String type; //course, laboratory, seminary, project

    @Column
    private int semester;

    @Column
    private int attendanceTotal;

    @Column
    private int absencesAllowed;
}
