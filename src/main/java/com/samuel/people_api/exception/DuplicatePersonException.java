package com.samuel.people_api.exception;

public class DuplicatePersonException extends RuntimeException {
    public DuplicatePersonException(String field) {
        super("Já existe uma pessoa cadastrada com este " + field);
    }
}
