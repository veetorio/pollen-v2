package com.pollen.comentario.internal.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ComentarioResponse(
        UUID id,
        String conteudo,
        UUID projetoId,
        UUID autorId,
        String autorNome,
        LocalDateTime criadoEm
) {}
