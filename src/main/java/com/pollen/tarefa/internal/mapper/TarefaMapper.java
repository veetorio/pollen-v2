package com.pollen.tarefa.internal.mapper;

import com.pollen.tarefa.internal.Tarefa;
import com.pollen.tarefa.internal.dto.TarefaResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TarefaMapper {

    default TarefaResponse toResponse(Tarefa tarefa) {
        if (tarefa == null) return null;

        List<TarefaResponse> subtarefas = tarefa.getSubtarefas() == null
                ? List.of()
                : tarefa.getSubtarefas().stream().map(this::toResponse).toList();

        return TarefaResponse.builder()
                .id(tarefa.getId())
                .titulo(tarefa.getTitulo())
                .projetoId(tarefa.getProjeto() != null ? tarefa.getProjeto().getId() : null)
                .tarefaPaiId(tarefa.getTarefaPai() != null ? tarefa.getTarefaPai().getId() : null)
                .prazoInicio(tarefa.getPrazo() != null ? tarefa.getPrazo().getInicio() : null)
                .prazoFim(tarefa.getPrazo() != null ? tarefa.getPrazo().getFim() : null)
                .progresso(tarefa.getAndamento() != null ? tarefa.getAndamento().getProgresso() : null)
                .status(tarefa.getAndamento() != null && tarefa.getAndamento().getStatus() != null
                        ? tarefa.getAndamento().getStatus().name() : null)
                .subtarefas(subtarefas)
                .build();
    }
}
