package com.newsense.backend.term.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "term", uniqueConstraints = @UniqueConstraint(name = "uk_term_name", columnNames = "name"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(nullable = false, length = 100)
    private String source;

    public static Term create(String name, String definition, String source) {
        Term term = new Term();
        term.name = name;
        term.definition = definition;
        term.source = source;
        return term;
    }
}
