package com.pollen.projeto.internal.mapper;

import com.pollen.projeto.Projeto;
import com.pollen.projeto.internal.dto.AndamentoDTO;
import com.pollen.projeto.internal.dto.PrazoDTO;
import com.pollen.projeto.internal.dto.ProjetoResponse;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.embeddable.Prazo;
import com.pollen.shared.enums.Status;
import org.mapstruct.Mapper;

@Mapper
public interface ProjetoMapper {

    default ProjetoResponse toResponse(Projeto projeto) {
        if (projeto == null) return null;

        var responsaveis = projeto.getResponsaveis().stream()
                .map(c -> ProjetoResponse.ResponsavelResumo.builder()
                        .matricula(c.getMatricula())
                        .nome(c.getNome())
                        .build())
                .toList();

        return ProjetoResponse.builder()
                .id(projeto.getId())
                .nome(projeto.getNome())
                .descricao(projeto.getDescricao())
                .classificacao(projeto.getClassificacao() != null
                        ? projeto.getClassificacao().name() : null)
                .teamId(projeto.getTeam() != null
                        ? projeto.getTeam().getIdentificador() : null)
                .teamNome(projeto.getTeam() != null
                        ? projeto.getTeam().getNome() : null)
                .responsaveis(responsaveis)
                .prazo(toPrazoDTO(projeto.getPrazo()))
                .andamento(toAndamentoDTO(projeto.getAndamento()))
                .build();
    }

    default PrazoDTO toPrazoDTO(Prazo prazo) {
        if (prazo == null) return null;
        return new PrazoDTO(prazo.getInicio(), prazo.getFim());
    }

    default Prazo toPrazo(PrazoDTO dto) {
        if (dto == null) return null;
        return Prazo.builder().inicio(dto.inicio()).fim(dto.fim()).build();
    }

    default AndamentoDTO toAndamentoDTO(Andamento andamento) {
        if (andamento == null) return null;
        return new AndamentoDTO(
                andamento.getProgresso(),
                andamento.getStatus() != null ? andamento.getStatus().name() : null
        );
    }

    default Andamento toAndamento(AndamentoDTO dto) {
        if (dto == null) return null;
        return Andamento.builder()
                .progresso(dto.progresso())
                .status(dto.status() != null ? Status.valueOf(dto.status()) : null)
                .build();
    }
}
