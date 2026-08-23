package com.pollen.usuario.internal.mapper;

import com.pollen.shared.embeddable.Contato;
import com.pollen.usuario.Usuario;
import com.pollen.usuario.internal.Administrador;
import com.pollen.usuario.internal.Colaborador;
import com.pollen.usuario.internal.Gestor;
import com.pollen.usuario.internal.dto.ContatoDTO;
import com.pollen.usuario.internal.dto.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface UsuarioMapper {

    default UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) return null;

        String tipo = switch (usuario) {
            case Administrador a -> "ADMINISTRADOR";
            case Gestor g -> "GESTOR";
            case Colaborador c -> "COLABORADOR";
            default -> "DESCONHECIDO";
        };

        String setor = (usuario instanceof Colaborador c) ? c.getSetor() : null;

        return UsuarioResponse.builder()
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .tipo(tipo)
                .setor(setor)
                .atividade(usuario.getAtividade())
                .contato(toContatoDTO(usuario.getContato()))
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }

    @Mapping(source = "emailSecundario", target = "emailSecundario")
    ContatoDTO toContatoDTO(Contato contato);

    @Mapping(source = "emailSecundario", target = "emailSecundario")
    Contato toContato(ContatoDTO dto);


}
