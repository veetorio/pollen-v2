package com.pollen.tarefa.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para atualização de status de uma tarefa.
 * RN17 — Estados possíveis: PENDENTE, PROGREDINDO, CONCLUIDO.
 */
public record AtualizarStatusRequest(

        @NotBlank(message = "Status é obrigatório")
        @Pattern(regexp = "PENDENTE|PROGREDINDO|CONCLUIDO",
                 message = "Status deve ser PENDENTE, PROGREDINDO ou CONCLUIDO")
        String status
) {}
