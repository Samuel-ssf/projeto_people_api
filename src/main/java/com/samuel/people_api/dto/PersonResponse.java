package com.samuel.people_api.dto;

import com.samuel.people_api.model.Person;

public record PersonResponse(Long id, String document, String name, String lastName, String email) {

    public static PersonResponse from(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getDocument(),
                person.getName(),
                person.getLastName(),
                person.getEmail());
    }
}
