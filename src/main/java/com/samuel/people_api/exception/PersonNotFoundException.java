package com.samuel.people_api.exception;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(Long id) {
        super("Pessoa com id " + id + " não encontrada");
    }
}
