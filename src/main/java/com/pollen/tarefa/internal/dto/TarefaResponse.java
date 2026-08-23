package com.pollen.tarefa.internal.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record TarefaResponse(
        UUID id,
        String titulo,
        UUID projetoId,
        UUID tarefaPaiId,
        LocalDateTime prazoInicio,
        LocalDateTime prazoFim,
        Float progresso,
        String status,
        List<TarefaResponse> subtarefas
) {}
