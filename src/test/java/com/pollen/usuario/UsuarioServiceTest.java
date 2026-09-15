package com.pollen.usuario;

import com.pollen.usuario.internal.Administrador;
import com.pollen.usuario.internal.Colaborador;
import com.pollen.usuario.internal.ColaboradorRepository;
import com.pollen.usuario.internal.Gestor;
import com.pollen.usuario.internal.GestorRepository;
import com.pollen.usuario.internal.UsuarioRepository;
import com.pollen.usuario.internal.dto.AtualizarUsuarioRequest;
import com.pollen.usuario.internal.dto.CriarUsuarioRequest;
import com.pollen.usuario.internal.dto.UsuarioResponse;
import com.pollen.usuario.internal.mapper.UsuarioMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do UsuarioService.
 *
 * Regras cobertas:
 * RN01 — Hierarquia de usuários (Admin, Gestor, Colaborador)
 * RN02 — Usuario como entidade base
 * RN03 — Administrador como nível máximo
 * RN06 — Exclusão física (sem controle de acesso nesta fase — documentado)
 * RF02 — Gerenciamento de usuários
 * RF04 — Gerenciamento de perfil
 * RF32 — Exclusão de perfil
 * RD04 — Colaboradores podem existir sem colmeia
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Regras de Negócio")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private GestorRepository gestorRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private UUID matriculaFixo;
    private UsuarioResponse responseFixo;

    @BeforeEach
    void setUp() {
        matriculaFixo = UUID.randomUUID();
        responseFixo = UsuarioResponse.builder()
                .matricula(matriculaFixo)
                .nome("Test User")
                .tipo("COLABORADOR")
                .atividade(true)
                .build();
    }

    // -----------------------------------------------------------------------
    // RN01 / RN02 — Criação de usuários por tipo
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criar() — RN01, RN02, RF02")
    class CriarUsuario {

        @Test
        @DisplayName("deve criar Administrador quando tipo=ADMINISTRADOR")
        void deveCriarAdministrador() {
            // Arrange
            var request = new CriarUsuarioRequest("Ana Admin", "senha123", "ADMINISTRADOR", null, null);
            var admin = Administrador.builder().nome("Ana Admin").senha("senha123").atividade(true).build();
            var adminResponse = UsuarioResponse.builder().nome("Ana Admin").tipo("ADMINISTRADOR").atividade(true).build();

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(admin);
            when(usuarioMapper.toResponse(admin)).thenReturn(adminResponse);

            // Act
            UsuarioResponse response = usuarioService.criar(request);

            // Assert
            assertThat(response.tipo()).isEqualTo("ADMINISTRADOR");
            assertThat(response.atividade()).isTrue();

            // Verifica que o tipo persisted é Administrador
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(Administrador.class);
        }

        @Test
        @DisplayName("deve criar Gestor quando tipo=GESTOR")
        void deveCriarGestor() {
            var request = new CriarUsuarioRequest("João Gestor", "senha123", "GESTOR", null, null);
            var gestor = Gestor.builder().nome("João Gestor").senha("senha123").atividade(true).build();
            var gestorResponse = UsuarioResponse.builder().nome("João Gestor").tipo("GESTOR").atividade(true).build();

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(gestor);
            when(usuarioMapper.toResponse(gestor)).thenReturn(gestorResponse);

            UsuarioResponse response = usuarioService.criar(request);

            assertThat(response.tipo()).isEqualTo("GESTOR");
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(Gestor.class);
        }

        @Test
        @DisplayName("deve criar Colaborador quando tipo=COLABORADOR — RD04 (existe sem colmeia)")
        void deveCriarColaborador() {
            var request = new CriarUsuarioRequest("Maria Colab", "senha123", "COLABORADOR", "DESENVOLVIMENTO", null);
            var colab = Colaborador.builder().nome("Maria Colab").senha("senha123").atividade(true).setor("DESENVOLVIMENTO").build();
            var colabResponse = UsuarioResponse.builder().nome("Maria Colab").tipo("COLABORADOR").setor("DESENVOLVIMENTO").atividade(true).build();

            when(usuarioRepository.save(any(Usuario.class))).thenReturn(colab);
            when(usuarioMapper.toResponse(colab)).thenReturn(colabResponse);

            UsuarioResponse response = usuarioService.criar(request);

            assertThat(response.tipo()).isEqualTo("COLABORADOR");
            assertThat(response.setor()).isEqualTo("DESENVOLVIMENTO");

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(Colaborador.class);
            // Colaborador criado sem colmeia — RD04
            assertThat(((Colaborador) captor.getValue()).getColmeias()).isEmpty();
        }

        @Test
        @DisplayName("deve setar atividade=true no cadastro de qualquer tipo")
        void deveSetarAtividadeTrue() {
            var request = new CriarUsuarioRequest("X", "y", "ADMINISTRADOR", null, null);
            var admin = Administrador.builder().nome("X").senha("y").atividade(true).build();
            when(usuarioRepository.save(any())).thenReturn(admin);
            when(usuarioMapper.toResponse(admin)).thenReturn(responseFixo);

            usuarioService.criar(request);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getAtividade()).isTrue();
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para tipo inválido")
        void deveLancarExcecaoParaTipoInvalido() {
            var request = new CriarUsuarioRequest("X", "y", "DIRETOR", null, null);

            assertThatThrownBy(() -> usuarioService.criar(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Tipo de usuário inválido");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // RF02 — Consulta e listagem
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("buscarPorId() e listar() — RF02")
    class ConsultaUsuario {

        @Test
        @DisplayName("deve retornar usuário quando matrícula existe")
        void deveRetornarUsuarioPorId() {
            var colaborador = Colaborador.builder().nome("X").senha("y").atividade(true).build();
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.of(colaborador));
            when(usuarioMapper.toResponse(colaborador)).thenReturn(responseFixo);

            UsuarioResponse response = usuarioService.buscarPorId(matriculaFixo);

            assertThat(response).isNotNull();
            verify(usuarioRepository).findById(matriculaFixo);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando matrícula não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarPorId(matriculaFixo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }

        @Test
        @DisplayName("deve retornar lista de todos os usuários")
        void deveListarTodosOsUsuarios() {
            var admin = Administrador.builder().nome("A").senha("s").atividade(true).build();
            var gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();
            when(usuarioRepository.findAll()).thenReturn(List.of(admin, gestor));
            when(usuarioMapper.toResponse(any())).thenReturn(responseFixo);

            List<UsuarioResponse> lista = usuarioService.listar();

            assertThat(lista).hasSize(2);
        }
    }

    // -----------------------------------------------------------------------
    // RF04 — Atualização de perfil
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("atualizar() — RF04")
    class AtualizarUsuario {

        @Test
        @DisplayName("deve atualizar nome de qualquer perfil")
        void deveAtualizarNome() {
            var colaborador = Colaborador.builder().nome("Antigo").senha("s").atividade(true).build();
            var request = new AtualizarUsuarioRequest("Novo Nome", null, null);
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.of(colaborador));
            when(usuarioRepository.save(any())).thenReturn(colaborador);
            when(usuarioMapper.toResponse(any())).thenReturn(responseFixo);

            usuarioService.atualizar(matriculaFixo, request);

            assertThat(colaborador.getNome()).isEqualTo("Novo Nome");
        }

        @Test
        @DisplayName("deve atualizar setor apenas para Colaborador")
        void deveAtualizarSetorDeColaborador() {
            var colaborador = Colaborador.builder().nome("X").senha("s").atividade(true).setor("DESIGN").build();
            var request = new AtualizarUsuarioRequest("X", "BACKEND", null);
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.of(colaborador));
            when(usuarioRepository.save(any())).thenReturn(colaborador);
            when(usuarioMapper.toResponse(any())).thenReturn(responseFixo);

            usuarioService.atualizar(matriculaFixo, request);

            assertThat(colaborador.getSetor()).isEqualTo("BACKEND");
        }

        @Test
        @DisplayName("deve ignorar campo setor quando usuário não é Colaborador")
        void deveIgnorarSetorParaNaoColaborador() {
            var gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();
            var request = new AtualizarUsuarioRequest("G", "QUALQUER", null);
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.of(gestor));
            when(usuarioRepository.save(any())).thenReturn(gestor);
            when(usuarioMapper.toResponse(any())).thenReturn(responseFixo);

            // Não deve lançar exceção — setor é ignorado para Gestor/Admin
            usuarioService.atualizar(matriculaFixo, request);

            verify(usuarioRepository).save(gestor);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar ID inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(usuarioRepository.findById(matriculaFixo)).thenReturn(Optional.empty());
            var request = new AtualizarUsuarioRequest("X", null, null);

            assertThatThrownBy(() -> usuarioService.atualizar(matriculaFixo, request))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RF32 / RN06 — Exclusão física
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("excluir() — RF32, RN06")
    class ExcluirUsuario {

        @Test
        @DisplayName("deve excluir usuário existente — RF32")
        void deveExcluirUsuarioExistente() {
            when(usuarioRepository.existsById(matriculaFixo)).thenReturn(true);

            usuarioService.excluir(matriculaFixo);

            verify(usuarioRepository).deleteById(matriculaFixo);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao excluir ID inexistente")
        void deveLancarExcecaoAoExcluirInexistente() {
            when(usuarioRepository.existsById(matriculaFixo)).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.excluir(matriculaFixo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(usuarioRepository, never()).deleteById(any());
        }
    }

    // -----------------------------------------------------------------------
    // Utilitários expostos para outros módulos
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("buscarColaborador() / buscarGestor()")
    class BuscarPorTipo {

        @Test
        @DisplayName("deve retornar Colaborador quando existe")
        void deveRetornarColaborador() {
            var colab = Colaborador.builder().nome("C").senha("s").atividade(true).build();
            when(colaboradorRepository.findById(matriculaFixo)).thenReturn(Optional.of(colab));

            Colaborador resultado = usuarioService.buscarColaborador(matriculaFixo);

            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para Colaborador inexistente")
        void deveLancarExcecaoParaColaboradorInexistente() {
            when(colaboradorRepository.findById(matriculaFixo)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarColaborador(matriculaFixo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Colaborador não encontrado");
        }

        @Test
        @DisplayName("deve retornar Gestor quando existe")
        void deveRetornarGestor() {
            var gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();
            when(gestorRepository.findById(matriculaFixo)).thenReturn(Optional.of(gestor));

            Gestor resultado = usuarioService.buscarGestor(matriculaFixo);

            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para Gestor inexistente")
        void deveLancarExcecaoParaGestorInexistente() {
            when(gestorRepository.findById(matriculaFixo)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarGestor(matriculaFixo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Gestor não encontrado");
        }
    }

    // -----------------------------------------------------------------------
    // contarTodos()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("contarTodos() deve delegar para o repository")
    void deveContarTodos() {
        when(usuarioRepository.count()).thenReturn(5L);

        long total = usuarioService.contarTodos();

        assertThat(total).isEqualTo(5L);
    }
}
