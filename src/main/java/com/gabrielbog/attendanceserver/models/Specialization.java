package com.gabrielbog.attendanceserver.models;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name="Specializations")
@NoArgsConstructor
@Data
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String name;

    @Column
    private int maxYears;
}
