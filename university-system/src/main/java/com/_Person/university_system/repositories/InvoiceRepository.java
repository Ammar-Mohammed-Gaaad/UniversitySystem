package com._Person.university_system.repositories;

import com._Person.university_system.operations.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStudentId(Long studentId);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
}
