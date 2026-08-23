package com.pollen.usuario.internal.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record ContatoDTO(
        @Email String email,
        @Email String emailSecundario,
        String telefone,
        String telefoneSecundario
) {}
