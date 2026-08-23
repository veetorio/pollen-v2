package com.pollen.comentario.internal.mapper;

import com.pollen.comentario.internal.Comentario;
import com.pollen.comentario.internal.dto.ComentarioResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ComentarioMapper {

    default ComentarioResponse toResponse(Comentario comentario) {
        if (comentario == null) return null;
        return ComentarioResponse.builder()
                .id(comentario.getId())
                .conteudo(comentario.getConteudo())
                .projetoId(comentario.getProjeto() != null
                        ? comentario.getProjeto().getId() : null)
                .autorId(comentario.getAutor() != null
                        ? comentario.getAutor().getMatricula() : null)
                .autorNome(comentario.getAutor() != null
                        ? comentario.getAutor().getNome() : null)
                .criadoEm(comentario.getCriadoEm())
                .build();
    }
}
