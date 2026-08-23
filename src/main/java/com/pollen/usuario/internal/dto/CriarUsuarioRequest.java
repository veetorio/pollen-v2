package com.pollen.usuario.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de criação de usuário.
 * O campo 'tipo' define qual especialização será criada: ADMINISTRADOR, GESTOR ou COLABORADOR.
 */
public record CriarUsuarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @NotNull(message = "Tipo é obrigatório")
        @Pattern(regexp = "ADMINISTRADOR|GESTOR|COLABORADOR",
                 message = "Tipo deve ser ADMINISTRADOR, GESTOR ou COLABORADOR")
        String tipo,

        /**
         * Setor é aplicável apenas a Colaboradores.
         * Pendência P01: tipo Setor não está especificado como enum.
         */
        String setor,

        @Valid
        ContatoDTO contato
) {}
