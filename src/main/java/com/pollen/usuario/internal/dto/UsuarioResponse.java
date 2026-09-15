package com.pollen.usuario.internal.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record UsuarioResponse(
        UUID matricula,
        String nome,
        String tipo,
        String setor,
        Boolean atividade,
        ContatoDTO contato,
        LocalDate criadoEm,
        LocalDate atualizadoEm,
        TokenOutput acessToken
) { 
   public UsuarioResponse withAcessToken(TokenOutput tokenOutput) {
        return new UsuarioResponse(
                this.matricula,
                this.nome,
                this.tipo,
                this.setor,
                this.atividade,
                this.contato,
                this.criadoEm,
                this.atualizadoEm,
                tokenOutput
        );
    } 

}

