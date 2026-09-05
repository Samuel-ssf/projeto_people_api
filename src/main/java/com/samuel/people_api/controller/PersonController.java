package com.samuel.people_api.controller;

import com.samuel.people_api.dto.NationalityResponse;
import com.samuel.people_api.dto.PersonRequest;
import com.samuel.people_api.dto.PersonResponse;
import com.samuel.people_api.service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping
public class PersonController {
    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @PostMapping("/registrarName")
    public ResponseEntity<PersonResponse> register(@Valid @RequestBody PersonRequest request) {
        PersonResponse person = service.create(request);
        return ResponseEntity.created(URI.create("/list/" + person.id())).body(person);
    }

    @GetMapping("/list")
    public List<PersonResponse> list() {
        return service.findAll();
    }

    @GetMapping("/list/{id}")
    public PersonResponse findById(@PathVariable @Positive(message = "O id deve ser maior que zero") Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/list/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "O id deve ser maior que zero") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/findNacionalityByPerson/{id}")
    public NationalityResponse findNationality(
            @PathVariable @Positive(message = "O id deve ser maior que zero") Long id) {
        return service.findNationalityByPerson(id);
    }
}
