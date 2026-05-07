package com._Person.university_system.controllers;

import com._Person.university_system.operations.Book;
import com._Person.university_system.operations.BookLoan;
import com._Person.university_system.repositories.BookRepository;
import com._Person.university_system.repositories.BookLoanRepository;
import com._Person.university_system.repositories.StudentRepository;
import com._Person.university_system.repositories.LecturerRepository;
import com._Person.university_system.users.Student;
import com._Person.university_system.users.Lecturer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LibraryWebController {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public LibraryWebController(BookRepository bookRepository, BookLoanRepository bookLoanRepository, StudentRepository studentRepository, LecturerRepository lecturerRepository) {
        this.bookRepository = bookRepository;
        this.bookLoanRepository = bookLoanRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    // Global / Student Library view
    @GetMapping("/library")
    public String libraryCatalog(Model model, Principal principal) {
        model.addAttribute("books", bookRepository.findAll());
        if (principal != null) {
            List<BookLoan> myLoans = new ArrayList<>();
            studentRepository.findByEmail(principal.getName()).ifPresent(student -> {
                myLoans.addAll(bookLoanRepository.findByStudent_Id(student.getID()));
            });
            lecturerRepository.findByEmail(principal.getName()).ifPresent(lecturer -> {
                myLoans.addAll(bookLoanRepository.findByLecturer_Id(lecturer.getID()));
            });
            model.addAttribute("myLoans", myLoans);
        }
        return "library-catalog";
    }

    @PostMapping("/library/borrow")
    public String borrowBook(@RequestParam Long bookId, Principal principal, RedirectAttributes ra) {
        if (principal != null) {
            bookRepository.findById(bookId).ifPresent(book -> {
                if (book.getAvailableQuantity() > 0) {
                    Student student = studentRepository.findByEmail(principal.getName()).orElse(null);
                    Lecturer lecturer = lecturerRepository.findByEmail(principal.getName()).orElse(null);

                    if (student != null || lecturer != null) {
                        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
                        bookRepository.save(book);

                        BookLoan loan = new BookLoan();
                        loan.setBook(book);
                        loan.setStudent(student);
                        loan.setLecturer(lecturer);
                        loan.setBorrowDate(LocalDate.now());
                        loan.setDueDate(LocalDate.now().plusDays(14)); // 2 weeks default
                        loan.setStatus(BookLoan.LoanStatus.BORROWED);
                        bookLoanRepository.save(loan);

                        ra.addFlashAttribute("successMessage", "You have successfully borrowed '" + book.getTitle() + "'.");
                    }
                } else {
                    ra.addFlashAttribute("errorMessage", "Book is currently out of stock.");
                }
            });
        }
        return "redirect:/library";
    }

    // Admin endpoints
    @GetMapping("/admin/library")
    public String adminLibrary(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("loans", bookLoanRepository.findAll());
        return "admin-library";
    }

    @PostMapping("/admin/library/add")
    public String addBook(@RequestParam String title, @RequestParam String author, @RequestParam String isbn, @RequestParam String category, @RequestParam int quantity, RedirectAttributes ra) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setTotalQuantity(quantity);
        book.setAvailableQuantity(quantity);
        bookRepository.save(book);
        ra.addFlashAttribute("successMessage", "Book added successfully!");
        return "redirect:/admin/library";
    }

    @PostMapping("/admin/library/return")
    public String returnBook(@RequestParam Long loanId, RedirectAttributes ra) {
        bookLoanRepository.findById(loanId).ifPresent(loan -> {
            if (loan.getStatus() == BookLoan.LoanStatus.BORROWED) {
                loan.setStatus(BookLoan.LoanStatus.RETURNED);
                loan.setReturnDate(LocalDate.now());
                bookLoanRepository.save(loan);

                Book book = loan.getBook();
                book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                bookRepository.save(book);
                ra.addFlashAttribute("successMessage", "Book returned successfully.");
            }
        });
        return "redirect:/admin/library";
    }
}
