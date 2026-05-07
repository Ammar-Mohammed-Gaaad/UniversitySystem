package com._Person.university_system.repositories;

import com._Person.university_system.operations.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {
    List<BookLoan> findByStudent_Id(Long studentId);
    List<BookLoan> findByLecturer_Id(Long lecturerId);
    List<BookLoan> findByStatus(BookLoan.LoanStatus status);
}
