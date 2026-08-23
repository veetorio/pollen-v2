package com.pollen.projeto.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarProjetoRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        @NotNull(message = "Classificação é obrigatória")
        String classificacao,

        PrazoDTO prazo
) {}
