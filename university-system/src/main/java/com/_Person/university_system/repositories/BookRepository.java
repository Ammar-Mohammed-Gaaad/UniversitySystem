package com._Person.university_system.repositories;

import com._Person.university_system.operations.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
