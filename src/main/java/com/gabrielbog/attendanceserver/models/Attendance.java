package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.sql.Time;
import java.sql.Date;

@Entity
@Table(name="Attendance")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int studentId;

    @Column
    private int scheduleId;

    @Column
    private Date scanDate;

    @Column
    private Time scanTime;
}
