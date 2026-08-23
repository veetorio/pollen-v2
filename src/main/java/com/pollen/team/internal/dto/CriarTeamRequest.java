package com.pollen.team.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarTeamRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        @NotNull(message = "Classificação é obrigatória")
        String classificacao,

        /**
         * UUID do Gestor responsável pela criação da colmeia.
         * RN08 — Um Gestor pode criar uma ou mais colmeias.
         */
        @NotNull(message = "ID do gestor é obrigatório")
        UUID gestorId
) {}
