package com.pollen.team;

import com.pollen.shared.enums.Classificacao;
import com.pollen.team.internal.TeamRepository;
import com.pollen.team.internal.dto.AtualizarTeamRequest;
import com.pollen.team.internal.dto.CriarTeamRequest;
import com.pollen.team.internal.dto.TeamResponse;
import com.pollen.team.internal.mapper.TeamMapper;
import com.pollen.usuario.UsuarioService;
import com.pollen.usuario.internal.Colaborador;
import com.pollen.usuario.internal.Gestor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * API pública do módulo team.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final UsuarioService usuarioService;

    /**
     * Cria uma nova colmeia vinculada a um Gestor.
     * RF05 / RN08 — Um Gestor pode criar uma ou mais colmeias.
     */
    @Transactional
    public TeamResponse criar(CriarTeamRequest request) {
        Gestor gestor = usuarioService.buscarGestor(request.gestorId());
        Classificacao classificacao = Classificacao.valueOf(request.classificacao());

        Team team = Team.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .classificacao(classificacao)
                .gestor(gestor)
                .build();

        return teamMapper.toResponse(teamRepository.save(team));
    }

    /**
     * Lista todas as colmeias.
     * RF06 — Consultar colmeias.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> listar() {
        return teamRepository.findAll()
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    /**
     * Consulta uma colmeia por ID.
     * RF06 — Consultar colmeias.
     */
    @Transactional(readOnly = true)
    public TeamResponse buscarPorId(UUID id) {
        return teamRepository.findById(id)
                .map(teamMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Colmeia não encontrada: " + id));
    }

    /**
     * Atualiza os dados de uma colmeia.
     * RF07 / RF08 — Atualizar colmeia e classificação.
     */
    @Transactional
    public TeamResponse atualizar(UUID id, AtualizarTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Colmeia não encontrada: " + id));

        team.setNome(request.nome());
        team.setDescricao(request.descricao());
        if (request.classificacao() != null) {
            team.setClassificacao(Classificacao.valueOf(request.classificacao()));
        }

        return teamMapper.toResponse(teamRepository.save(team));
    }

    /**
     * Exclui uma colmeia.
     */
    @Transactional
    public void excluir(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new EntityNotFoundException("Colmeia não encontrada: " + id);
        }
        teamRepository.deleteById(id);
    }

    /**
     * Adiciona um colaborador a uma colmeia.
     * RF09 / RN10 — O Gestor pode adicionar colaboradores manualmente.
     */
    @Transactional
    public TeamResponse adicionarColaborador(UUID teamId, UUID colaboradorId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Colmeia não encontrada: " + teamId));
        Colaborador colaborador = usuarioService.buscarColaborador(colaboradorId);

        if (!team.getColaboradores().contains(colaborador)) {
            team.getColaboradores().add(colaborador);
        }

        return teamMapper.toResponse(teamRepository.save(team));
    }

    /**
     * Remove um colaborador de uma colmeia.
     * RF11 — O Gestor pode remover um colaborador de uma colmeia.
     */
    @Transactional
    public TeamResponse removerColaborador(UUID teamId, UUID colaboradorId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Colmeia não encontrada: " + teamId));

        team.getColaboradores().removeIf(c -> c.getMatricula().equals(colaboradorId));
        return teamMapper.toResponse(teamRepository.save(team));
    }

    /**
     * Busca entidade Team por ID — exposta para outros módulos.
     */
    @Transactional(readOnly = true)
    public Team buscarEntidade(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Colmeia não encontrada: " + id));
    }

    /** Contagem total de colmeias — exposto para o módulo admin. */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return teamRepository.count();
    }
}
