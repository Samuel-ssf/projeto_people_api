package com.samuel.people_api.dto;

public record NationalityResponse(Long personId, String personName, String countryCode, String nationality) {
}
