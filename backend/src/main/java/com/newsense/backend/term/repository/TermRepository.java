package com.newsense.backend.term.repository;

import com.newsense.backend.term.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByName(String name);
}
