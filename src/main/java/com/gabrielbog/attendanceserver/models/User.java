package com.gabrielbog.attendanceserver.models;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name="Users")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(columnDefinition = "integer default 0")
    private int isAdmin;

    @Column
    private String cnp;

    @Column
    private String password;

    @Column
    private String firstName;

    @Column
    private String lastName;

}