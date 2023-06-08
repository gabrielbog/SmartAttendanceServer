package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="Scancodes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Scancode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int subjectId;

    @Column
    private int scheduleId;

    @Column
    private String code; //sha1-hashed code of schedule, professor and creation timestamp

    @Column
    private Date creationDate; //the code will be valid only for a day

    @Column
    private Time timeGenerated; //used when refreshing the code

    @Column
    private Time timeStart; //for easier table operations

    @Column
    private Time timeStop; //for easier table operations
}
