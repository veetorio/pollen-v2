package com.pollen.comentario.internal.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CriarComentarioRequest(

        @NotBlank(message = "Conteúdo é obrigatório")
        String conteudo,

        /** UUID do autor. Pendência P02: obrigatoriedade a definir. */
        UUID autorId
) {}
