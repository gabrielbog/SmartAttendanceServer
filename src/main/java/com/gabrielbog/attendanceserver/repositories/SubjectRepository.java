package com.gabrielbog.attendanceserver.repositories;

import com.gabrielbog.attendanceserver.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    List<Subject> findBySpecAndGrade(int spec, int grade);
}
