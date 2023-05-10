package com.gabrielbog.attendanceserver.models;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Entity
@Table(name="Users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(columnDefinition = "integer default 0")
    private int isAdmin;

    @Column(unique = true)
    private String cnp;

    @Column
    private String password; //encrypted

    @Column
    private String firstName;

    @Column
    private String lastName;

}