package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByProfessorId(int professorId);
}
