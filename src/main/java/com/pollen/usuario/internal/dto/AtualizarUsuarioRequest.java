package com.pollen.usuario.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record AtualizarUsuarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String setor,

        @Valid
        ContatoDTO contato
) {}
