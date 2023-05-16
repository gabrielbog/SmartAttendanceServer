package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Scancode;
import com.gabrielbog.attendanceserver.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScancodeRepository extends JpaRepository<Scancode, Integer> {
    Optional<Scancode> findByCode(String code);
}
