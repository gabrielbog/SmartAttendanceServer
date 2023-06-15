package com.gabrielbog.attendanceserver.models;

import lombok.*;

import jakarta.persistence.*;

import java.sql.Time;

@Entity
@Table(name="Schedules")
@NoArgsConstructor
@Data
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
    private int weekday; //0 - Sunday, 6 - Saturday

    @Column
    private int studentGrup; //student group; 0 - all groups

    @Column
    private String room;
}