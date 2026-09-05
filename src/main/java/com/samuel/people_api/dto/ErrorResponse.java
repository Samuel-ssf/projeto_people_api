package com.samuel.people_api.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(Instant timestamp, int status, String message, Map<String, String> errors) {
}
