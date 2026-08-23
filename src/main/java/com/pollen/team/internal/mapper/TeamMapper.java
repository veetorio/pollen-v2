package com.pollen.team.internal.mapper;

import com.pollen.team.Team;
import com.pollen.team.internal.dto.TeamResponse;
import com.pollen.usuario.internal.Colaborador;
import org.mapstruct.Mapper;

@Mapper
public interface TeamMapper {

    default TeamResponse toResponse(Team team) {
        if (team == null) return null;

        var colaboradores = team.getColaboradores().stream()
                .map(c -> TeamResponse.ColaboradorResumo.builder()
                        .matricula(c.getMatricula())
                        .nome(c.getNome())
                        .setor(c.getSetor())
                        .build())
                .toList();

        return TeamResponse.builder()
                .identificador(team.getIdentificador())
                .nome(team.getNome())
                .descricao(team.getDescricao())
                .classificacao(team.getClassificacao() != null
                        ? team.getClassificacao().name() : null)
                .gestorId(team.getGestor() != null
                        ? team.getGestor().getMatricula() : null)
                .gestorNome(team.getGestor() != null
                        ? team.getGestor().getNome() : null)
                .colaboradores(colaboradores)
                .criadaEm(team.getCriadaEm())
                .atualizadoEm(team.getAtualizadoEm())
                .build();
    }
}
