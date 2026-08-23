package com.pollen.projeto;

import com.pollen.projeto.internal.ProjetoRepository;
import com.pollen.projeto.internal.dto.AtualizarProjetoRequest;
import com.pollen.projeto.internal.dto.CriarProjetoRequest;
import com.pollen.projeto.internal.dto.ProjetoResponse;
import com.pollen.projeto.internal.mapper.ProjetoMapper;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.embeddable.Prazo;
import com.pollen.shared.enums.Classificacao;
import com.pollen.shared.enums.Status;
import com.pollen.team.Team;
import com.pollen.team.TeamService;
import com.pollen.usuario.UsuarioService;
import com.pollen.usuario.internal.Colaborador;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * API pública do módulo projeto.
 */
@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMapper projetoMapper;
    private final TeamService teamService;
    private final UsuarioService usuarioService;

    /**
     * Cria um projeto dentro de uma colmeia.
     * RF12 / RN24 — Projeto deve estar associado a uma colmeia.
     */
    @Transactional
    public ProjetoResponse criar(UUID teamId, CriarProjetoRequest request) {
        Team team = teamService.buscarEntidade(teamId);

        Projeto projeto = Projeto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .classificacao(Classificacao.valueOf(request.classificacao()))
                .team(team)
                .prazo(projetoMapper.toPrazo(request.prazo()))
                .andamento(Andamento.builder()
                        .progresso(0f)
                        .status(Status.PENDENTE)
                        .build())
                .build();

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    /**
     * Lista projetos de uma colmeia.
     * RF13 — Consultar projetos.
     */
    @Transactional(readOnly = true)
    public List<ProjetoResponse> listarPorTeam(UUID teamId) {
        return projetoRepository.findByTeamIdentificador(teamId)
                .stream()
                .map(projetoMapper::toResponse)
                .toList();
    }

    /**
     * Consulta projeto por ID.
     * RF13 — Consultar projetos.
     */
    @Transactional(readOnly = true)
    public ProjetoResponse buscarPorId(UUID id) {
        return projetoRepository.findById(id)
                .map(projetoMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + id));
    }

    /**
     * Atualiza dados, prazo e andamento de um projeto.
     * RF14 / RF16 / RF17 — Atualizar projeto, progresso e prazo.
     */
    @Transactional
    public ProjetoResponse atualizar(UUID id, AtualizarProjetoRequest request) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + id));

        projeto.setNome(request.nome());
        projeto.setDescricao(request.descricao());

        if (request.classificacao() != null) {
            projeto.setClassificacao(Classificacao.valueOf(request.classificacao()));
        }
        if (request.prazo() != null) {
            projeto.setPrazo(projetoMapper.toPrazo(request.prazo()));
        }
        if (request.andamento() != null) {
            projeto.setAndamento(projetoMapper.toAndamento(request.andamento()));
        }

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    /**
     * Exclui um projeto.
     */
    @Transactional
    public void excluir(UUID id) {
        if (!projetoRepository.existsById(id)) {
            throw new EntityNotFoundException("Projeto não encontrado: " + id);
        }
        projetoRepository.deleteById(id);
    }

    /**
     * Associa colaboradores como responsáveis pelo projeto.
     * RF15 / RN25 — Um projeto pode possuir um ou mais colaboradores responsáveis.
     */
    @Transactional
    public ProjetoResponse associarResponsaveis(UUID projetoId, List<UUID> colaboradorIds) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + projetoId));

        List<Colaborador> responsaveis = colaboradorIds.stream()
                .map(usuarioService::buscarColaborador)
                .toList();

        projeto.getResponsaveis().clear();
        projeto.getResponsaveis().addAll(responsaveis);

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    /**
     * Recalcula o progresso do projeto com base no andamento das tarefas.
     * Acionado via evento TarefaConcluidaEvent (Fase 7).
     * Por ora recebe o valor diretamente.
     */
    @Transactional
    public void recalcularProgresso(UUID projetoId) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + projetoId));

        long total = projeto.getTarefas().size();
        if (total == 0) return;

        long concluidas = projeto.getTarefas().stream()
                .filter(t -> t.getAndamento() != null
                        && Status.CONCLUIDO.equals(t.getAndamento().getStatus()))
                .count();

        float progresso = (float) concluidas / total;
        Status status = concluidas == total ? Status.CONCLUIDO
                : concluidas > 0 ? Status.PROGREDINDO
                : Status.PENDENTE;

        projeto.setAndamento(Andamento.builder()
                .progresso(progresso)
                .status(status)
                .build());

        projetoRepository.save(projeto);
    }

    /**
     * Retorna a entidade Projeto — exposta para outros módulos.
     */
    @Transactional(readOnly = true)
    public Projeto buscarEntidade(UUID id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + id));
    }

    /** Contagem total de projetos — exposto para o módulo admin. */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return projetoRepository.count();
    }
}
