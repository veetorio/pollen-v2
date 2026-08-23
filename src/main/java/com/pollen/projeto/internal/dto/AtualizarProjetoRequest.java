package com.pollen.projeto.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarProjetoRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        String classificacao,

        PrazoDTO prazo,

        AndamentoDTO andamento
) {}
