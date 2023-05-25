package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.sql.Time;

@Entity
@Table(name="Schedules")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int subjectId;

    @Column
    private int professorId;

    @Column
    private Time timeStart;

    @Column
    private Time timeStop;

    @Column
    private int weekday; //0 - Monday, 6 - Sunday

    @Column
    private int studentGrade; //student year - kindof useless

    @Column
    private int studentGrup; //student group; 0 - all groups

    @Column
    private String room;
}