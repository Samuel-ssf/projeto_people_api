package com.samuel.people_api.repository;

import com.samuel.people_api.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByDocument(String document);

    boolean existsByEmail(String email);
}