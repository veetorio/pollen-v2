package com.pollen.team.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarTeamRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        String classificacao
) {}
