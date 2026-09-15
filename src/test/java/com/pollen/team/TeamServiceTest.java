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
 * Testes unitários do TeamService.
 *
 * Regras cobertas:
 * RN07 — Responsabilidade do Gestor
 * RN08 — Um Gestor pode criar uma ou mais colmeias
 * RN09 — Administração de colmeias
 * RN10 — Adição de colaboradores (manual)
 * RN18 — Classificação das colmeias (OCULTA / PUBLICA)
 * RN19 — Colmeia oculta
 * RN20 — Colmeia pública
 * RN21 — Colmeia pode existir sem colaboradores (RD03)
 * RN22 — Colaborador pode não estar em colmeia (RD04)
 * RN23 — Colaborador pode estar em múltiplas colmeias
 * RF05 — Criar colmeia
 * RF06 — Consultar colmeias
 * RF07 — Atualizar colmeia
 * RF08 — Classificar colmeia
 * RF09 — Adicionar colaborador
 * RF11 — Remover colaborador
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService — Regras de Negócio")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TeamService teamService;

    private UUID teamId;
    private UUID gestorId;
    private UUID colaboradorId;
    private Gestor gestorFixo;
    private Colaborador colaboradorFixo;
    private Team teamFixo;
    private TeamResponse responseFixo;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        gestorId = UUID.randomUUID();
        colaboradorId = UUID.randomUUID();

        gestorFixo = Gestor.builder().nome("Gestor Teste").senha("s").atividade(true).build();

        colaboradorFixo = Colaborador.builder().nome("Colab Teste").senha("s").atividade(true).build();

        teamFixo = Team.builder()
                .nome("Equipe Dev")
                .descricao("Desc")
                .classificacao(Classificacao.PUBLICA)
                .gestor(gestorFixo)
                .build();

        responseFixo = TeamResponse.builder()
                .identificador(teamId)
                .nome("Equipe Dev")
                .classificacao("PUBLICA")
                .build();
    }

    // -----------------------------------------------------------------------
    // RN08 — Criação de colmeia vinculada a um Gestor
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criar() — RN08, RF05")
    class CriarTeam {

        @Test
        @DisplayName("deve criar colmeia PUBLICA vinculada ao Gestor — RN08, RN20")
        void deveCriarColmeiaPublica() {
            var request = new CriarTeamRequest("Equipe Dev", "Desc", "PUBLICA", gestorId);
            when(usuarioService.buscarGestor(gestorId)).thenReturn(gestorFixo);
            when(teamRepository.save(any(Team.class))).thenReturn(teamFixo);
            when(teamMapper.toResponse(teamFixo)).thenReturn(responseFixo);

            TeamResponse response = teamService.criar(request);

            assertThat(response).isNotNull();
            assertThat(response.classificacao()).isEqualTo("PUBLICA");

            ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(captor.capture());
            assertThat(captor.getValue().getGestor()).isEqualTo(gestorFixo);
            assertThat(captor.getValue().getClassificacao()).isEqualTo(Classificacao.PUBLICA);
        }

        @Test
        @DisplayName("deve criar colmeia OCULTA — RN19")
        void deveCriarColmeiaOculta() {
            var request = new CriarTeamRequest("Equipe Secreta", "Desc", "OCULTA", gestorId);
            var teamOculta = Team.builder()
                    .nome("Equipe Secreta")
                    .classificacao(Classificacao.OCULTA)
                    .gestor(gestorFixo)
                    .build();
            var responseOculta = TeamResponse.builder().nome("Equipe Secreta").classificacao("OCULTA").build();

            when(usuarioService.buscarGestor(gestorId)).thenReturn(gestorFixo);
            when(teamRepository.save(any(Team.class))).thenReturn(teamOculta);
            when(teamMapper.toResponse(teamOculta)).thenReturn(responseOculta);

            TeamResponse response = teamService.criar(request);

            assertThat(response.classificacao()).isEqualTo("OCULTA");

            ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(captor.capture());
            assertThat(captor.getValue().getClassificacao()).isEqualTo(Classificacao.OCULTA);
        }

        @Test
        @DisplayName("deve criar colmeia sem colaboradores — RN21 / RD03")
        void deveCriarColmeiaSemColaboradores() {
            var request = new CriarTeamRequest("Equipe Vazia", null, "PUBLICA", gestorId);
            when(usuarioService.buscarGestor(gestorId)).thenReturn(gestorFixo);
            when(teamRepository.save(any(Team.class))).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.criar(request);

            ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
            verify(teamRepository).save(captor.capture());
            // Colmeia recém-criada não deve ter colaboradores — RN21
            assertThat(captor.getValue().getColaboradores()).isEmpty();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se Gestor não existe")
        void deveLancarExcecaoSeGestorNaoExiste() {
            var request = new CriarTeamRequest("X", null, "PUBLICA", gestorId);
            when(usuarioService.buscarGestor(gestorId))
                    .thenThrow(new EntityNotFoundException("Gestor não encontrado: " + gestorId));

            assertThatThrownBy(() -> teamService.criar(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Gestor não encontrado");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para classificação inválida")
        void deveLancarExcecaoParaClassificacaoInvalida() {
            var request = new CriarTeamRequest("X", null, "INVALIDA", gestorId);
            when(usuarioService.buscarGestor(gestorId)).thenReturn(gestorFixo);

            assertThatThrownBy(() -> teamService.criar(request))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(teamRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // RF06 — Consulta
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("buscarPorId() / listar() — RF06")
    class ConsultarTeam {

        @Test
        @DisplayName("deve retornar colmeia quando ID existe")
        void deveRetornarColmeia() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(teamMapper.toResponse(teamFixo)).thenReturn(responseFixo);

            TeamResponse response = teamService.buscarPorId(teamId);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando ID não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.buscarPorId(teamId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Colmeia não encontrada");
        }

        @Test
        @DisplayName("deve retornar lista de todas as colmeias")
        void deveListarTodas() {
            when(teamRepository.findAll()).thenReturn(List.of(teamFixo));
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            List<TeamResponse> lista = teamService.listar();

            assertThat(lista).hasSize(1);
        }
    }

    // -----------------------------------------------------------------------
    // RF07 / RF08 — Atualização e reclassificação
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("atualizar() — RF07, RF08, RN09")
    class AtualizarTeam {

        @Test
        @DisplayName("deve atualizar nome, descrição e classificação da colmeia")
        void deveAtualizarColmeia() {
            var request = new AtualizarTeamRequest("Novo Nome", "Nova Desc", "OCULTA");
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.atualizar(teamId, request);

            assertThat(teamFixo.getNome()).isEqualTo("Novo Nome");
            assertThat(teamFixo.getDescricao()).isEqualTo("Nova Desc");
            assertThat(teamFixo.getClassificacao()).isEqualTo(Classificacao.OCULTA);
        }

        @Test
        @DisplayName("deve manter classificação original quando campo é null")
        void deveManterClassificacaoSeNull() {
            var request = new AtualizarTeamRequest("Novo Nome", null, null);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.atualizar(teamId, request);

            // Classificação não deve ser alterada
            assertThat(teamFixo.getClassificacao()).isEqualTo(Classificacao.PUBLICA);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar colmeia inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.atualizar(teamId, new AtualizarTeamRequest("X", null, null)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF09 / RN10 — Adição de colaboradores
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("adicionarColaborador() — RF09, RN10")
    class AdicionarColaborador {

        @Test
        @DisplayName("deve adicionar colaborador à colmeia — RN10, RN23")
        void deveAdicionarColaborador() {
            teamFixo.getColaboradores().clear();
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(usuarioService.buscarColaborador(colaboradorId)).thenReturn(colaboradorFixo);
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.adicionarColaborador(teamId, colaboradorId);

            assertThat(teamFixo.getColaboradores()).contains(colaboradorFixo);
        }

        @Test
        @DisplayName("não deve adicionar colaborador duplicado à mesma colmeia")
        void naoDeveAdicionarColaboradorDuplicado() {
            teamFixo.getColaboradores().add(colaboradorFixo);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(usuarioService.buscarColaborador(colaboradorId)).thenReturn(colaboradorFixo);
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.adicionarColaborador(teamId, colaboradorId);

            // Deve continuar com apenas 1 — sem duplicata
            assertThat(teamFixo.getColaboradores()).hasSize(1);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se colmeia não existe ao adicionar")
        void deveLancarExcecaoSeColmeiaNaoExiste() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.adicionarColaborador(teamId, colaboradorId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Colmeia não encontrada");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se colaborador não existe")
        void deveLancarExcecaoSeColaboradorNaoExiste() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(usuarioService.buscarColaborador(colaboradorId))
                    .thenThrow(new EntityNotFoundException("Colaborador não encontrado: " + colaboradorId));

            assertThatThrownBy(() -> teamService.adicionarColaborador(teamId, colaboradorId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Colaborador não encontrado");
        }
    }

    // -----------------------------------------------------------------------
    // RF11 — Remoção de colaboradores
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("removerColaborador() — RF11")
    class RemoverColaborador {

        @Test
        @DisplayName("deve remover colaborador existente da colmeia")
        void deveRemoverColaborador() {
            // Força uma matrícula conhecida no colaborador
            UUID matriculaColab = UUID.randomUUID();
            Colaborador colaboradorComId = spy(colaboradorFixo);
            doReturn(matriculaColab).when(colaboradorComId).getMatricula();

            teamFixo.getColaboradores().add(colaboradorComId);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            teamService.removerColaborador(teamId, matriculaColab);

            assertThat(teamFixo.getColaboradores()).isEmpty();
        }

        @Test
        @DisplayName("não deve falhar ao remover colaborador que não está na colmeia")
        void naoDeveFalharAoRemoverColaboradorAusente() {
            teamFixo.getColaboradores().clear();
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamFixo));
            when(teamRepository.save(any())).thenReturn(teamFixo);
            when(teamMapper.toResponse(any())).thenReturn(responseFixo);

            // removeIf com UUID inexistente não deve lançar exceção
            teamService.removerColaborador(teamId, UUID.randomUUID());

            verify(teamRepository).save(teamFixo);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se colmeia não existe ao remover")
        void deveLancarExcecaoAoRemoverDeColmeiaInexistente() {
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.removerColaborador(teamId, colaboradorId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // Exclusão de colmeia
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("excluir() — colmeia")
    class ExcluirTeam {

        @Test
        @DisplayName("deve excluir colmeia existente")
        void deveExcluirColmeia() {
            when(teamRepository.existsById(teamId)).thenReturn(true);

            teamService.excluir(teamId);

            verify(teamRepository).deleteById(teamId);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao excluir colmeia inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(teamRepository.existsById(teamId)).thenReturn(false);

            assertThatThrownBy(() -> teamService.excluir(teamId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(teamRepository, never()).deleteById(any());
        }
    }

    // -----------------------------------------------------------------------
    // contarTodos()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("contarTodos() deve delegar para o repository")
    void deveContarTodos() {
        when(teamRepository.count()).thenReturn(3L);

        long total = teamService.contarTodos();

        assertThat(total).isEqualTo(3L);
    }
}
