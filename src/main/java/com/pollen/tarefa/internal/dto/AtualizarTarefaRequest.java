package com.pollen.tarefa.internal.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record AtualizarTarefaRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        LocalDateTime prazoInicio,
        LocalDateTime prazoFim
) {}
