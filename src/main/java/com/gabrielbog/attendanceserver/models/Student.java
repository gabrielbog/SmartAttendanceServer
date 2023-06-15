package com.gabrielbog.attendanceserver.models;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name="Students")
@NoArgsConstructor
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(unique = true)
    private int userId;

    @Column
    private int spec;

    @Column
    private int grade; //year

    @Column
    private int grup; //group
}