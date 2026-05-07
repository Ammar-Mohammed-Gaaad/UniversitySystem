package com._Person.university_system.controllers;

import com._Person.university_system.operations.Invoice;
import com._Person.university_system.operations.Payment;
import com._Person.university_system.repositories.InvoiceRepository;
import com._Person.university_system.repositories.PaymentRepository;
import com._Person.university_system.repositories.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class FinanceWebController {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    public FinanceWebController(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, StudentRepository studentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
    }

    // Admin endpoints
    @GetMapping("/admin/finances")
    public String adminFinances(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        return "admin-finances";
    }

    @PostMapping("/admin/finances/invoice")
    public String generateInvoice(@RequestParam Long studentId, @RequestParam String description, @RequestParam BigDecimal amount, @RequestParam String dueDate, RedirectAttributes ra) {
        studentRepository.findById(studentId).ifPresent(student -> {
            Invoice invoice = new Invoice();
            invoice.setStudent(student);
            invoice.setDescription(description);
            invoice.setAmount(amount);
            invoice.setDueDate(LocalDate.parse(dueDate));
            invoice.setStatus(Invoice.InvoiceStatus.PENDING);
            invoiceRepository.save(invoice);
            ra.addFlashAttribute("successMessage", "Invoice generated successfully for " + student.getName());
        });
        return "redirect:/admin/finances";
    }

    // Student endpoints
    @GetMapping("/student/finances")
    public String studentFinances(Model model, Principal principal) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                List<Invoice> invoices = invoiceRepository.findByStudentId(student.getID());
                model.addAttribute("invoices", invoices);
                
                BigDecimal totalDue = invoices.stream()
                    .filter(i -> i.getStatus() != Invoice.InvoiceStatus.PAID)
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("totalDue", totalDue);
            });
        }
        return "student-finances";
    }

    @PostMapping("/student/finances/pay")
    public String payInvoice(@RequestParam Long invoiceId, @RequestParam BigDecimal amount, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
                    if (invoice.getStudent().getID().equals(student.getID()) && invoice.getStatus() != Invoice.InvoiceStatus.PAID) {
                        Payment payment = new Payment();
                        payment.setInvoice(invoice);
                        payment.setAmount(amount);
                        payment.setPaymentDate(LocalDateTime.now());
                        payment.setTransactionReference("TXN-" + System.currentTimeMillis());
                        paymentRepository.save(payment);

                        // If fully paid
                        // In a real system, we'd sum all payments to check. For simplicity, we just check this payment amount.
                        if (amount.compareTo(invoice.getAmount()) >= 0) {
                            invoice.setStatus(Invoice.InvoiceStatus.PAID);
                        }
                        invoiceRepository.save(invoice);
                        ra.addFlashAttribute("successMessage", "Payment of $" + amount + " successful!");
                    }
                });
            });
        }
        return "redirect:/student/finances";
    }
}
