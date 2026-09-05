package com.samuel.people_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonRequest(
        @NotBlank(message = "Documento é obrigatório")
        @Pattern(regexp = "\\d{7,12}", message = "Documento deve conter entre 7 e 12 dígitos numéricos")
        String document,
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String name,
        @NotBlank(message = "Sobrenome é obrigatório")
        @Size(max = 100, message = "Sobrenome deve ter no máximo 100 caracteres")
        String lastName,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve possuir um formato válido")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
        String email) {
}
