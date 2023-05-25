package com.gabrielbog.attendanceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AttendanceserverApplication {

	public static void main(String[] args) {

		AttendanceCalendar attendanceCalendar = AttendanceCalendar.getInstance(); //read calendar for getting attendance properly
		SpringApplication.run(AttendanceserverApplication.class, args);
	}
}