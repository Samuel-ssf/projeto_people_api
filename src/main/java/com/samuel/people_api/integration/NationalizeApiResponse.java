package com.samuel.people_api.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NationalizeApiResponse(List<CountryProbability> country) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryProbability(String country_id, double probability) {
    }
}
