package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Attendance;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByScanDateAndScheduleId(Date scanDate, int scheduleId);
    List<Attendance> findByStudentIdAndSubjectId(int studentId, int subjectId);
}
