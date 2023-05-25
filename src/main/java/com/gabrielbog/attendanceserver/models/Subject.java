package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Entity
@Table(name="Subjects")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
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
