package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Scancode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface ScancodeRepository extends JpaRepository<Scancode, Integer> {
    Optional<Scancode> findByCode(String code);
    Optional<Scancode> findByCreationDateAndScheduleId(Date creationDate, int scheduleId);
}
