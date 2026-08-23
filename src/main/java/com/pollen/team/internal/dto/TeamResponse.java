package com.pollen.team.internal.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record TeamResponse(
        UUID identificador,
        String nome,
        String descricao,
        String classificacao,
        UUID gestorId,
        String gestorNome,
        List<ColaboradorResumo> colaboradores,
        LocalDate criadaEm,
        LocalDate atualizadoEm
) {
    @Builder
    public record ColaboradorResumo(UUID matricula, String nome, String setor) {}
}
