package com.pollen.projeto.internal.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProjetoResponse(
        UUID id,
        String nome,
        String descricao,
        String classificacao,
        UUID teamId,
        String teamNome,
        List<ResponsavelResumo> responsaveis,
        PrazoDTO prazo,
        AndamentoDTO andamento
) {
    @Builder
    public record ResponsavelResumo(UUID matricula, String nome) {}
}
