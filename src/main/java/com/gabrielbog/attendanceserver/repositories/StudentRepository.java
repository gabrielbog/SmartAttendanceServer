package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Student;
import com.gabrielbog.attendanceserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByUserId(int userId);
    List<Student> findByGradeAndSpec(int grade, int spec);
}
