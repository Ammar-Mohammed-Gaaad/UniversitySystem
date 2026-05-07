package com._Person.university_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com._Person.university_system.AcademicStructure.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
