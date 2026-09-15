package com.pollen.projeto;

import com.pollen.projeto.internal.ProjetoRepository;
import com.pollen.projeto.internal.dto.AtualizarProjetoRequest;
import com.pollen.projeto.internal.dto.CriarProjetoRequest;
import com.pollen.projeto.internal.dto.ProjetoResponse;
import com.pollen.projeto.internal.mapper.ProjetoMapper;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.enums.Classificacao;
import com.pollen.shared.enums.Status;
import com.pollen.tarefa.internal.Tarefa;
import com.pollen.team.Team;
import com.pollen.team.TeamService;
import com.pollen.usuario.UsuarioService;
import com.pollen.usuario.internal.Colaborador;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ProjetoService.
 *
 * Regras cobertas:
 * RN11 — Gestão de projetos pelo Gestor
 * RN13 — Acompanhamento de projetos
 * RN24 — Projeto deve estar associado a uma colmeia
 * RN25 — Responsáveis pelo projeto
 * RN26 — Projeto deve ter andamento (progresso + status)
 * RF12 — Criar projeto
 * RF13 — Consultar projetos
 * RF14 — Atualizar projeto
 * RF15 — Associar colaboradores
 * RF16 — Acompanhar progresso
 * RF17 — Definir prazo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjetoService — Regras de Negócio")
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private ProjetoMapper projetoMapper;

    @Mock
    private TeamService teamService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ProjetoService projetoService;

    private UUID projetoId;
    private UUID teamId;
    private Team teamFixo;
    private Projeto projetoFixo;
    private ProjetoResponse responseFixo;

    @BeforeEach
    void setUp() {
        projetoId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        Gestor gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();

        teamFixo = Team.builder()
                .nome("Team Teste")
                .classificacao(Classificacao.PUBLICA)
                .gestor(gestor)
                .build();

        projetoFixo = Projeto.builder()
                .nome("Projeto Teste")
                .descricao("Desc")
                .classificacao(Classificacao.PUBLICA)
                .team(teamFixo)
                .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                .build();

        responseFixo = ProjetoResponse.builder()
                .id(projetoId)
                .nome("Projeto Teste")
                .build();
    }

    // -----------------------------------------------------------------------
    // RN24 — Criação de projeto vinculado a uma colmeia
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criar() — RN24, RN26, RF12")
    class CriarProjeto {

        @Test
        @DisplayName("deve criar projeto vinculado a uma colmeia com andamento PENDENTE inicial — RN24, RN26")
        void deveCriarProjetoVinculadoAColmeia() {
            var request = new CriarProjetoRequest("Projeto Teste", "Desc", "PUBLICA", null);
            when(teamService.buscarEntidade(teamId)).thenReturn(teamFixo);
            when(projetoRepository.save(any(Projeto.class))).thenReturn(projetoFixo);
            when(projetoMapper.toResponse(projetoFixo)).thenReturn(responseFixo);

            projetoService.criar(teamId, request);

            ArgumentCaptor<Projeto> captor = ArgumentCaptor.forClass(Projeto.class);
            verify(projetoRepository).save(captor.capture());

            Projeto salvo = captor.getValue();
            // RN24 — projeto vinculado à colmeia
            assertThat(salvo.getTeam()).isEqualTo(teamFixo);
            // RN26 — andamento inicial deve ser PENDENTE com 0%
            assertThat(salvo.getAndamento().getStatus()).isEqualTo(Status.PENDENTE);
            assertThat(salvo.getAndamento().getProgresso()).isEqualTo(0f);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se colmeia não existe — RN24")
        void deveLancarExcecaoSeColmeiaNaoExiste() {
            var request = new CriarProjetoRequest("X", null, "PUBLICA", null);
            when(teamService.buscarEntidade(teamId))
                    .thenThrow(new EntityNotFoundException("Colmeia não encontrada: " + teamId));

            assertThatThrownBy(() -> projetoService.criar(teamId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Colmeia não encontrada");

            verify(projetoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para classificação inválida")
        void deveLancarExcecaoParaClassificacaoInvalida() {
            var request = new CriarProjetoRequest("X", null, "ERRADA", null);
            when(teamService.buscarEntidade(teamId)).thenReturn(teamFixo);

            assertThatThrownBy(() -> projetoService.criar(teamId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF13 — Consulta de projetos
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("buscarPorId() — RF13")
    class ConsultarProjeto {

        @Test
        @DisplayName("deve retornar projeto quando ID existe")
        void deveRetornarProjeto() {
            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(projetoMapper.toResponse(projetoFixo)).thenReturn(responseFixo);

            ProjetoResponse response = projetoService.buscarPorId(projetoId);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando projeto não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projetoService.buscarPorId(projetoId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Projeto não encontrado");
        }
    }

    // -----------------------------------------------------------------------
    // RF14 / RF17 — Atualização de projeto e prazo
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("atualizar() — RF14, RF17")
    class AtualizarProjeto {

        @Test
        @DisplayName("deve atualizar nome e descrição do projeto")
        void deveAtualizarProjeto() {
            var request = new AtualizarProjetoRequest("Novo Nome", "Nova Desc", null, null, null);
            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(projetoRepository.save(any())).thenReturn(projetoFixo);
            when(projetoMapper.toResponse(any())).thenReturn(responseFixo);

            projetoService.atualizar(projetoId, request);

            assertThat(projetoFixo.getNome()).isEqualTo("Novo Nome");
            assertThat(projetoFixo.getDescricao()).isEqualTo("Nova Desc");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar projeto inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projetoService.atualizar(projetoId,
                    new AtualizarProjetoRequest("X", null, null, null, null)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF15 / RN25 — Associação de responsáveis
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("associarResponsaveis() — RF15, RN25")
    class AssociarResponsaveis {

        @Test
        @DisplayName("deve associar colaboradores como responsáveis — RN25")
        void deveAssociarResponsaveis() {
            UUID c1Id = UUID.randomUUID();
            UUID c2Id = UUID.randomUUID();
            Colaborador c1 = Colaborador.builder().nome("C1").senha("s").atividade(true).build();
            Colaborador c2 = Colaborador.builder().nome("C2").senha("s").atividade(true).build();

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(usuarioService.buscarColaborador(c1Id)).thenReturn(c1);
            when(usuarioService.buscarColaborador(c2Id)).thenReturn(c2);
            when(projetoRepository.save(any())).thenReturn(projetoFixo);
            when(projetoMapper.toResponse(any())).thenReturn(responseFixo);

            projetoService.associarResponsaveis(projetoId, List.of(c1Id, c2Id));

            assertThat(projetoFixo.getResponsaveis()).containsExactlyInAnyOrder(c1, c2);
        }

        @Test
        @DisplayName("deve substituir responsáveis anteriores ao reassociar")
        void deveSubstituirResponsaveis() {
            Colaborador antigo = Colaborador.builder().nome("Antigo").senha("s").atividade(true).build();
            projetoFixo.getResponsaveis().add(antigo);

            UUID novoId = UUID.randomUUID();
            Colaborador novo = Colaborador.builder().nome("Novo").senha("s").atividade(true).build();

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(usuarioService.buscarColaborador(novoId)).thenReturn(novo);
            when(projetoRepository.save(any())).thenReturn(projetoFixo);
            when(projetoMapper.toResponse(any())).thenReturn(responseFixo);

            projetoService.associarResponsaveis(projetoId, List.of(novoId));

            assertThat(projetoFixo.getResponsaveis()).doesNotContain(antigo);
            assertThat(projetoFixo.getResponsaveis()).contains(novo);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se colaborador não existe")
        void deveLancarExcecaoParaColaboradorInexistente() {
            UUID idInexistente = UUID.randomUUID();
            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(usuarioService.buscarColaborador(idInexistente))
                    .thenThrow(new EntityNotFoundException("Colaborador não encontrado: " + idInexistente));

            assertThatThrownBy(() -> projetoService.associarResponsaveis(projetoId, List.of(idInexistente)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF16 / RN26 — Recalcular progresso
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("recalcularProgresso() — RF16, RN26")
    class RecalcularProgresso {

        @Test
        @DisplayName("deve calcular progresso 0 quando nenhuma tarefa está concluída")
        void deveCalcularProgressoZeroSemTarefasConcluidas() {
            Tarefa t1 = criarTarefa(Status.PENDENTE);
            Tarefa t2 = criarTarefa(Status.PROGREDINDO);
            projetoFixo.getTarefas().addAll(List.of(t1, t2));

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(projetoRepository.save(any())).thenReturn(projetoFixo);

            projetoService.recalcularProgresso(projetoId);

            assertThat(projetoFixo.getAndamento().getProgresso()).isEqualTo(0f);
            assertThat(projetoFixo.getAndamento().getStatus()).isEqualTo(Status.PENDENTE);
        }

        @Test
        @DisplayName("deve calcular progresso 0.5 quando metade das tarefas está concluída")
        void deveCalcularProgressoParcial() {
            Tarefa concluida = criarTarefa(Status.CONCLUIDO);
            Tarefa pendente = criarTarefa(Status.PENDENTE);
            projetoFixo.getTarefas().addAll(List.of(concluida, pendente));

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(projetoRepository.save(any())).thenReturn(projetoFixo);

            projetoService.recalcularProgresso(projetoId);

            assertThat(projetoFixo.getAndamento().getProgresso()).isEqualTo(0.5f);
            assertThat(projetoFixo.getAndamento().getStatus()).isEqualTo(Status.PROGREDINDO);
        }

        @Test
        @DisplayName("deve calcular progresso 1.0 quando todas as tarefas estão concluídas")
        void deveCalcularProgressoTotal() {
            Tarefa t1 = criarTarefa(Status.CONCLUIDO);
            Tarefa t2 = criarTarefa(Status.CONCLUIDO);
            projetoFixo.getTarefas().addAll(List.of(t1, t2));

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));
            when(projetoRepository.save(any())).thenReturn(projetoFixo);

            projetoService.recalcularProgresso(projetoId);

            assertThat(projetoFixo.getAndamento().getProgresso()).isEqualTo(1.0f);
            assertThat(projetoFixo.getAndamento().getStatus()).isEqualTo(Status.CONCLUIDO);
        }

        @Test
        @DisplayName("não deve alterar andamento quando projeto não tem tarefas")
        void naoDeveAlterarAndamentoSemTarefas() {
            projetoFixo.getTarefas().clear();
            Andamento andamentoOriginal = projetoFixo.getAndamento();

            when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projetoFixo));

            projetoService.recalcularProgresso(projetoId);

            assertThat(projetoFixo.getAndamento()).isEqualTo(andamentoOriginal);
            verify(projetoRepository, never()).save(any());
        }

        private Tarefa criarTarefa(Status status) {
            Tarefa t = Tarefa.builder()
                    .titulo("Tarefa")
                    .projeto(projetoFixo)
                    .andamento(Andamento.builder().progresso(
                            status == Status.CONCLUIDO ? 1f : 0f).status(status).build())
                    .build();
            return t;
        }
    }

    // -----------------------------------------------------------------------
    // Exclusão
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("excluir() — projeto")
    class ExcluirProjeto {

        @Test
        @DisplayName("deve excluir projeto existente")
        void deveExcluirProjeto() {
            when(projetoRepository.existsById(projetoId)).thenReturn(true);

            projetoService.excluir(projetoId);

            verify(projetoRepository).deleteById(projetoId);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao excluir projeto inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(projetoRepository.existsById(projetoId)).thenReturn(false);

            assertThatThrownBy(() -> projetoService.excluir(projetoId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
