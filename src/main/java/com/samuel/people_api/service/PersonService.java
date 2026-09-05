package com.samuel.people_api.service;

import com.samuel.people_api.dto.NationalityResponse;
import com.samuel.people_api.dto.PersonRequest;
import com.samuel.people_api.dto.PersonResponse;
import com.samuel.people_api.exception.DuplicatePersonException;
import com.samuel.people_api.exception.ExternalServiceException;
import com.samuel.people_api.exception.PersonNotFoundException;
import com.samuel.people_api.integration.NationalizeApiResponse;
import com.samuel.people_api.integration.NationalizeClient;
import com.samuel.people_api.model.Person;
import com.samuel.people_api.repository.PersonRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {
    private final PersonRepository repository;
    private final NationalizeClient nationalizeClient;

    public PersonService(PersonRepository repository, NationalizeClient nationalizeClient) {
        this.repository = repository;
        this.nationalizeClient = nationalizeClient;
    }

    @Transactional
    public PersonResponse create(PersonRequest request) {
        if (repository.existsByDocument(request.document())) {
            throw new DuplicatePersonException("documento");
        }
        if (repository.existsByEmail(request.email())) {
            throw new DuplicatePersonException("e-mail");
        }
        Person person = new Person(request.document(), request.name(), request.lastName(), request.email());
        return PersonResponse.from(repository.save(person));
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> findAll() {
        return repository.findAll().stream().map(PersonResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PersonResponse findById(Long id) {
        return PersonResponse.from(findPerson(id));
    }

    @Transactional
    public void delete(Long id) {
        Person person = findPerson(id);
        repository.delete(person);
    }

    @Transactional(readOnly = true)
    public NationalityResponse findNationalityByPerson(Long id) {
        Person person = findPerson(id);
        NationalizeApiResponse response = nationalizeClient.findByName(person.getName());
        NationalizeApiResponse.CountryProbability country = response.country() == null ? null : response.country().stream()
                .filter(item -> item.country_id() != null && !item.country_id().isBlank())
                .max(Comparator.comparingDouble(NationalizeApiResponse.CountryProbability::probability))
                .orElse(null);
        if (country == null) {
            throw new ExternalServiceException("Não foi encontrada uma nacionalidade provável para esta pessoa");
        }

        String countryCode = country.country_id().toUpperCase(Locale.ROOT);
        String countryName = new Locale("", countryCode).getDisplayCountry(Locale.forLanguageTag("pt-BR"));
        if (countryName.isBlank() || countryName.equalsIgnoreCase(countryCode)) {
            throw new ExternalServiceException("A API retornou um código de país desconhecido");
        }
        return new NationalityResponse(person.getId(), person.getName(), countryCode, countryName);
    }

    private Person findPerson(Long id) {
        return repository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
    }
}
