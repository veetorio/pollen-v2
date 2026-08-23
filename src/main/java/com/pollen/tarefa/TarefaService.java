package com.pollen.tarefa;

import com.pollen.projeto.Projeto;
import com.pollen.projeto.ProjetoService;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.embeddable.Prazo;
import com.pollen.shared.enums.Status;
import com.pollen.tarefa.internal.Tarefa;
import com.pollen.tarefa.internal.TarefaRepository;
import com.pollen.tarefa.internal.dto.AtualizarStatusRequest;
import com.pollen.tarefa.internal.dto.AtualizarTarefaRequest;
import com.pollen.tarefa.internal.dto.CriarTarefaRequest;
import com.pollen.tarefa.internal.dto.TarefaResponse;
import com.pollen.tarefa.internal.mapper.TarefaMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * API pública do módulo tarefa.
 */
@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final TarefaMapper tarefaMapper;
    private final ProjetoService projetoService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Cria uma tarefa raiz em um projeto.
     * RF18 / RN27 — Tarefa deve estar relacionada a um projeto.
     */
    @Transactional
    public TarefaResponse criar(UUID projetoId, CriarTarefaRequest request) {
        Projeto projeto = projetoService.buscarEntidade(projetoId);

        Tarefa tarefa = Tarefa.builder()
                .titulo(request.titulo())
                .projeto(projeto)
                .prazo(buildPrazo(request.prazoInicio(), request.prazoFim()))
                .andamento(Andamento.builder()
                        .progresso(0f)
                        .status(Status.PENDENTE)
                        .build())
                .build();

        return tarefaMapper.toResponse(tarefaRepository.save(tarefa));
    }

    /**
     * Cria uma subtarefa vinculada a uma tarefa pai.
     * RF22 / RN28 / RB05 — Estrutura recursiva de tarefas.
     */
    @Transactional
    public TarefaResponse criarSubtarefa(UUID tarefaPaiId, CriarTarefaRequest request) {
        Tarefa pai = tarefaRepository.findById(tarefaPaiId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + tarefaPaiId));

        Tarefa subtarefa = Tarefa.builder()
                .titulo(request.titulo())
                .projeto(pai.getProjeto())
                .tarefaPai(pai)
                .prazo(buildPrazo(request.prazoInicio(), request.prazoFim()))
                .andamento(Andamento.builder()
                        .progresso(0f)
                        .status(Status.PENDENTE)
                        .build())
                .build();

        return tarefaMapper.toResponse(tarefaRepository.save(subtarefa));
    }

    /**
     * Lista tarefas raiz de um projeto (sem pai).
     * RF19 — Consultar tarefas.
     */
    @Transactional(readOnly = true)
    public List<TarefaResponse> listarPorProjeto(UUID projetoId) {
        return tarefaRepository.findByProjetoIdAndTarefaPaiIsNull(projetoId)
                .stream()
                .map(tarefaMapper::toResponse)
                .toList();
    }

    /**
     * Consulta uma tarefa por ID.
     * RF19 — Consultar tarefas.
     */
    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(UUID id) {
        return tarefaRepository.findById(id)
                .map(tarefaMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + id));
    }

    /**
     * Atualiza título e prazo de uma tarefa.
     * RF20 / RF21 — Atualizar tarefa e prazo.
     */
    @Transactional
    public TarefaResponse atualizar(UUID id, AtualizarTarefaRequest request) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + id));

        tarefa.setTitulo(request.titulo());
        tarefa.setPrazo(buildPrazo(request.prazoInicio(), request.prazoFim()));

        return tarefaMapper.toResponse(tarefaRepository.save(tarefa));
    }

    /**
     * Atualiza o status de uma tarefa.
     * RF24 / RN17 — Status: PENDENTE, PROGREDINDO, CONCLUIDO.
     * Ao concluir, publica TarefaConcluidaEvent para o módulo projeto recalcular progresso.
     */
    @Transactional
    public TarefaResponse atualizarStatus(UUID id, AtualizarStatusRequest request) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: " + id));

        Status novoStatus = Status.valueOf(request.status());
        float progresso = switch (novoStatus) {
            case CONCLUIDO   -> 1.0f;
            case PROGREDINDO -> tarefa.getAndamento() != null
                    ? tarefa.getAndamento().getProgresso() : 0f;
            case PENDENTE    -> 0f;
        };

        tarefa.setAndamento(Andamento.builder()
                .progresso(progresso)
                .status(novoStatus)
                .build());

        Tarefa salva = tarefaRepository.save(tarefa);

        if (Status.CONCLUIDO.equals(novoStatus) && salva.getProjeto() != null) {
            eventPublisher.publishEvent(
                    new TarefaConcluidaEvent(salva.getId(), salva.getProjeto().getId()));
        }

        return tarefaMapper.toResponse(salva);
    }

    /**
     * Exclui uma tarefa (e suas subtarefas em cascata).
     */
    @Transactional
    public void excluir(UUID id) {
        if (!tarefaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tarefa não encontrada: " + id);
        }
        tarefaRepository.deleteById(id);
    }

    /** Contagem total de tarefas — exposto para o módulo admin. */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return tarefaRepository.count();
    }

    private Prazo buildPrazo(java.time.LocalDateTime inicio, java.time.LocalDateTime fim) {
        if (inicio == null && fim == null) return null;
        return Prazo.builder().inicio(inicio).fim(fim).build();
    }
}
