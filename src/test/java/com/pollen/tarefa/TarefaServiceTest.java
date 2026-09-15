package com.pollen.tarefa;

import com.pollen.projeto.Projeto;
import com.pollen.projeto.ProjetoService;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.enums.Classificacao;
import com.pollen.shared.enums.Status;
import com.pollen.tarefa.internal.Tarefa;
import com.pollen.tarefa.internal.TarefaRepository;
import com.pollen.tarefa.internal.dto.AtualizarStatusRequest;
import com.pollen.tarefa.internal.dto.AtualizarTarefaRequest;
import com.pollen.tarefa.internal.dto.CriarTarefaRequest;
import com.pollen.tarefa.internal.dto.TarefaResponse;
import com.pollen.tarefa.internal.mapper.TarefaMapper;
import com.pollen.team.Team;
import com.pollen.usuario.internal.Gestor;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do TarefaService.
 *
 * Regras cobertas:
 * RN12 — Gestão de tarefas e subtarefas
 * RN16 — Execução de tarefas pelo Colaborador
 * RN17 — Alteração de status (PENDENTE, PROGREDINDO, CONCLUIDO)
 * RN27 — Tarefa deve pertencer a um projeto
 * RN28 — Estrutura recursiva de subtarefas
 * RF18 — Criar tarefa
 * RF19 — Consultar tarefas
 * RF20 — Atualizar tarefa
 * RF21 — Definir prazo da tarefa
 * RF22 — Criar subtarefas
 * RF23 — Atualizar progresso
 * RF24 — Atualizar status
 * RD05 — Tarefas hierárquicas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TarefaService — Regras de Negócio")
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private TarefaMapper tarefaMapper;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TarefaService tarefaService;

    private UUID projetoId;
    private UUID tarefaId;
    private Projeto projetoFixo;
    private Tarefa tarefaFixa;
    private TarefaResponse responseFixo;

    @BeforeEach
    void setUp() {
        projetoId = UUID.randomUUID();
        tarefaId = UUID.randomUUID();

        Gestor gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();
        Team team = Team.builder().nome("T").classificacao(Classificacao.PUBLICA).gestor(gestor).build();

        projetoFixo = Projeto.builder()
                .nome("Projeto Teste")
                .classificacao(Classificacao.PUBLICA)
                .team(team)
                .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                .build();

        tarefaFixa = Tarefa.builder()
                .titulo("Tarefa Teste")
                .projeto(projetoFixo)
                .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                .build();

        responseFixo = TarefaResponse.builder()
                .id(tarefaId)
                .titulo("Tarefa Teste")
                .status("PENDENTE")
                .progresso(0f)
                .build();
    }

    // -----------------------------------------------------------------------
    // RN27 — Criação de tarefa vinculada ao projeto
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criar() — RN27, RF18")
    class CriarTarefa {

        @Test
        @DisplayName("deve criar tarefa vinculada ao projeto com status PENDENTE inicial — RN27")
        void deveCriarTarefaVinculadaAoProjeto() {
            var request = new CriarTarefaRequest("Criar endpoints", null, null);
            when(projetoService.buscarEntidade(projetoId)).thenReturn(projetoFixo);
            when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(tarefaFixa)).thenReturn(responseFixo);

            tarefaService.criar(projetoId, request);

            ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
            verify(tarefaRepository).save(captor.capture());

            Tarefa salva = captor.getValue();
            // RN27 — vinculada ao projeto
            assertThat(salva.getProjeto()).isEqualTo(projetoFixo);
            // Status inicial deve ser PENDENTE
            assertThat(salva.getAndamento().getStatus()).isEqualTo(Status.PENDENTE);
            assertThat(salva.getAndamento().getProgresso()).isEqualTo(0f);
            // Tarefa raiz não tem pai
            assertThat(salva.getTarefaPai()).isNull();
        }

        @Test
        @DisplayName("deve criar tarefa com prazo quando fornecido — RF21")
        void deveCriarTarefaComPrazo() {
            LocalDateTime inicio = LocalDateTime.of(2026, 8, 22, 8, 0);
            LocalDateTime fim = LocalDateTime.of(2026, 9, 30, 18, 0);
            var request = new CriarTarefaRequest("Tarefa com prazo", inicio, fim);
            when(projetoService.buscarEntidade(projetoId)).thenReturn(projetoFixo);
            when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.criar(projetoId, request);

            ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
            verify(tarefaRepository).save(captor.capture());
            assertThat(captor.getValue().getPrazo()).isNotNull();
            assertThat(captor.getValue().getPrazo().getInicio()).isEqualTo(inicio);
            assertThat(captor.getValue().getPrazo().getFim()).isEqualTo(fim);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se projeto não existe — RN27")
        void deveLancarExcecaoSeProjetoNaoExiste() {
            var request = new CriarTarefaRequest("X", null, null);
            when(projetoService.buscarEntidade(projetoId))
                    .thenThrow(new EntityNotFoundException("Projeto não encontrado: " + projetoId));

            assertThatThrownBy(() -> tarefaService.criar(projetoId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Projeto não encontrado");

            verify(tarefaRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // RN28 / RD05 — Criação de subtarefas
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criarSubtarefa() — RN28, RF22, RD05")
    class CriarSubtarefa {

        @Test
        @DisplayName("deve criar subtarefa vinculada à tarefa pai — RN28")
        void deveCriarSubtarefaVinculadaAoPai() {
            var request = new CriarTarefaRequest("Subtarefa", null, null);
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.criarSubtarefa(tarefaId, request);

            ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
            verify(tarefaRepository).save(captor.capture());

            Tarefa subtarefa = captor.getValue();
            // Deve herdar o projeto da tarefa pai — RN27
            assertThat(subtarefa.getProjeto()).isEqualTo(projetoFixo);
            // Deve referenciar a tarefa pai — RN28
            assertThat(subtarefa.getTarefaPai()).isEqualTo(tarefaFixa);
            // Status inicial deve ser PENDENTE
            assertThat(subtarefa.getAndamento().getStatus()).isEqualTo(Status.PENDENTE);
        }

        @Test
        @DisplayName("deve criar subtarefa de subtarefa (recursividade) — RD05")
        void deveCriarSubtarefaDeSubtarefa() {
            // subtarefa também é uma tarefa — pode ter subtarefas próprias
            Tarefa subtarefaPai = Tarefa.builder()
                    .titulo("Subtarefa Pai")
                    .projeto(projetoFixo)
                    .tarefaPai(tarefaFixa)
                    .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                    .build();

            UUID subtarefaPaiId = UUID.randomUUID();
            var request = new CriarTarefaRequest("Sub-subtarefa", null, null);
            when(tarefaRepository.findById(subtarefaPaiId)).thenReturn(Optional.of(subtarefaPai));
            when(tarefaRepository.save(any())).thenReturn(subtarefaPai);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.criarSubtarefa(subtarefaPaiId, request);

            ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
            verify(tarefaRepository).save(captor.capture());
            assertThat(captor.getValue().getTarefaPai()).isEqualTo(subtarefaPai);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para tarefa pai inexistente")
        void deveLancarExcecaoParaTarefaPaiInexistente() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tarefaService.criarSubtarefa(tarefaId, new CriarTarefaRequest("X", null, null)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tarefa não encontrada");

            verify(tarefaRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // RN17 — Alteração de status (PENDENTE, PROGREDINDO, CONCLUIDO)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("atualizarStatus() — RN17, RF24")
    class AtualizarStatus {

        @Test
        @DisplayName("deve definir progresso=0.0 ao mudar status para PENDENTE — RN17")
        void deveDefinirProgressoZeroParaPendente() {
            tarefaFixa.setAndamento(Andamento.builder().progresso(0.5f).status(Status.PROGREDINDO).build());
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any())).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("PENDENTE"));

            assertThat(tarefaFixa.getAndamento().getStatus()).isEqualTo(Status.PENDENTE);
            assertThat(tarefaFixa.getAndamento().getProgresso()).isEqualTo(0f);
        }

        @Test
        @DisplayName("deve definir progresso=1.0 ao mudar status para CONCLUIDO — RN17")
        void deveDefinirProgressoUmParaConcluido() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any())).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("CONCLUIDO"));

            assertThat(tarefaFixa.getAndamento().getStatus()).isEqualTo(Status.CONCLUIDO);
            assertThat(tarefaFixa.getAndamento().getProgresso()).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("deve manter progresso atual ao mudar status para PROGREDINDO — RN17")
        void deveManterProgressoParaProgredindo() {
            tarefaFixa.setAndamento(Andamento.builder().progresso(0.3f).status(Status.PENDENTE).build());
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any())).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("PROGREDINDO"));

            assertThat(tarefaFixa.getAndamento().getStatus()).isEqualTo(Status.PROGREDINDO);
            assertThat(tarefaFixa.getAndamento().getProgresso()).isEqualTo(0.3f);
        }

        @Test
        @DisplayName("deve publicar TarefaConcluidaEvent ao concluir tarefa vinculada a projeto")
        void devePublicarEventoAoConcluir() {
            // Forçar ID no projeto via spy
            UUID idProjeto = UUID.randomUUID();
            Projeto projetoSpy = spy(projetoFixo);
            doReturn(idProjeto).when(projetoSpy).getId();

            Tarefa tarefaComProjeto = Tarefa.builder()
                    .titulo("T")
                    .projeto(projetoSpy)
                    .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                    .build();

            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaComProjeto));
            when(tarefaRepository.save(any())).thenReturn(tarefaComProjeto);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("CONCLUIDO"));

            ArgumentCaptor<TarefaConcluidaEvent> captor = ArgumentCaptor.forClass(TarefaConcluidaEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().projetoId()).isEqualTo(idProjeto);
        }

        @Test
        @DisplayName("não deve publicar TarefaConcluidaEvent para status != CONCLUIDO")
        void naoDevePublicarEventoParaStatusNaoConcluido() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any())).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("PROGREDINDO"));

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar status de tarefa inexistente")
        void deveLancarExcecaoParaTarefaInexistente() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tarefaService.atualizarStatus(tarefaId, new AtualizarStatusRequest("CONCLUIDO")))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF19 — Consulta
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("buscarPorId() / listarPorProjeto() — RF19")
    class ConsultarTarefa {

        @Test
        @DisplayName("deve retornar tarefa por ID")
        void deveRetornarTarefaPorId() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaMapper.toResponse(tarefaFixa)).thenReturn(responseFixo);

            TarefaResponse response = tarefaService.buscarPorId(tarefaId);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para tarefa inexistente")
        void deveLancarExcecaoParaTarefaInexistente() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tarefaService.buscarPorId(tarefaId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tarefa não encontrada");
        }

        @Test
        @DisplayName("deve listar apenas tarefas raiz do projeto (sem pai)")
        void deveListarTarefasRaiz() {
            when(tarefaRepository.findByProjetoIdAndTarefaPaiIsNull(projetoId))
                    .thenReturn(List.of(tarefaFixa));
            when(tarefaMapper.toResponse(tarefaFixa)).thenReturn(responseFixo);

            List<TarefaResponse> lista = tarefaService.listarPorProjeto(projetoId);

            assertThat(lista).hasSize(1);
            verify(tarefaRepository).findByProjetoIdAndTarefaPaiIsNull(projetoId);
        }
    }

    // -----------------------------------------------------------------------
    // RF20 / RF21 — Atualização de tarefa e prazo
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("atualizar() — RF20, RF21")
    class AtualizarTarefa {

        @Test
        @DisplayName("deve atualizar título da tarefa")
        void deveAtualizarTitulo() {
            var request = new AtualizarTarefaRequest("Novo Título", null, null);
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.of(tarefaFixa));
            when(tarefaRepository.save(any())).thenReturn(tarefaFixa);
            when(tarefaMapper.toResponse(any())).thenReturn(responseFixo);

            tarefaService.atualizar(tarefaId, request);

            assertThat(tarefaFixa.getTitulo()).isEqualTo("Novo Título");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar tarefa inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(tarefaRepository.findById(tarefaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tarefaService.atualizar(tarefaId, new AtualizarTarefaRequest("X", null, null)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // Exclusão
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("excluir() — tarefa")
    class ExcluirTarefa {

        @Test
        @DisplayName("deve excluir tarefa existente")
        void deveExcluirTarefa() {
            when(tarefaRepository.existsById(tarefaId)).thenReturn(true);

            tarefaService.excluir(tarefaId);

            verify(tarefaRepository).deleteById(tarefaId);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao excluir tarefa inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(tarefaRepository.existsById(tarefaId)).thenReturn(false);

            assertThatThrownBy(() -> tarefaService.excluir(tarefaId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
