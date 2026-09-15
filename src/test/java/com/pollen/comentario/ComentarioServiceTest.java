package com.pollen.comentario;

import com.pollen.comentario.internal.Comentario;
import com.pollen.comentario.internal.ComentarioRepository;
import com.pollen.comentario.internal.dto.ComentarioResponse;
import com.pollen.comentario.internal.dto.CriarComentarioRequest;
import com.pollen.comentario.internal.mapper.ComentarioMapper;
import com.pollen.projeto.Projeto;
import com.pollen.projeto.ProjetoService;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.enums.Classificacao;
import com.pollen.shared.enums.Status;
import com.pollen.team.Team;
import com.pollen.usuario.Usuario;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ComentarioService.
 *
 * Regras cobertas:
 * RF33 — Adicionar comentário em projetos
 * RF34 — Consultar comentários de um projeto
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComentarioService — Regras de Negócio")
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ComentarioMapper comentarioMapper;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ComentarioService comentarioService;

    private UUID projetoId;
    private UUID autorId;
    private Projeto projetoFixo;
    private Usuario autorFixo;
    private ComentarioResponse responseFixo;

    @BeforeEach
    void setUp() {
        projetoId = UUID.randomUUID();
        autorId = UUID.randomUUID();

        Gestor gestor = Gestor.builder().nome("G").senha("s").atividade(true).build();
        Team team = Team.builder().nome("T").classificacao(Classificacao.PUBLICA).gestor(gestor).build();

        projetoFixo = Projeto.builder()
                .nome("Projeto Comentado")
                .classificacao(Classificacao.PUBLICA)
                .team(team)
                .andamento(Andamento.builder().progresso(0f).status(Status.PENDENTE).build())
                .build();

        autorFixo = Colaborador.builder().nome("Autor").senha("s").atividade(true).build();

        responseFixo = ComentarioResponse.builder()
                .id(UUID.randomUUID())
                .conteudo("Comentário de teste")
                .build();
    }

    // -----------------------------------------------------------------------
    // RF33 — Adição de comentário
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("criar() — RF33")
    class CriarComentario {

        @Test
        @DisplayName("deve criar comentário com autor informado — RF33")
        void deveCriarComentarioComAutor() {
            var request = new CriarComentarioRequest("Bom progresso!", autorId);
            var comentario = Comentario.builder()
                    .conteudo("Bom progresso!")
                    .projeto(projetoFixo)
                    .autor(autorFixo)
                    .build();

            when(projetoService.buscarEntidade(projetoId)).thenReturn(projetoFixo);
            when(usuarioService.buscarPorEntidade(autorId)).thenReturn(autorFixo);
            when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);
            when(comentarioMapper.toResponse(comentario)).thenReturn(responseFixo);

            ComentarioResponse response = comentarioService.criar(projetoId, request);

            assertThat(response).isNotNull();

            ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
            verify(comentarioRepository).save(captor.capture());
            assertThat(captor.getValue().getProjeto()).isEqualTo(projetoFixo);
            assertThat(captor.getValue().getAutor()).isEqualTo(autorFixo);
            assertThat(captor.getValue().getConteudo()).isEqualTo("Bom progresso!");
        }

        @Test
        @DisplayName("deve criar comentário sem autor (autor opcional — Pendência P02)")
        void deveCriarComentarioSemAutor() {
            var request = new CriarComentarioRequest("Comentário anônimo", null);
            var comentario = Comentario.builder()
                    .conteudo("Comentário anônimo")
                    .projeto(projetoFixo)
                    .autor(null)
                    .build();

            when(projetoService.buscarEntidade(projetoId)).thenReturn(projetoFixo);
            when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);
            when(comentarioMapper.toResponse(comentario)).thenReturn(responseFixo);

            comentarioService.criar(projetoId, request);

            ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
            verify(comentarioRepository).save(captor.capture());
            // Autor deve ser null — não deve tentar buscar usuário
            verify(usuarioService, never()).buscarPorEntidade(any());
            assertThat(captor.getValue().getAutor()).isNull();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se projeto não existe")
        void deveLancarExcecaoSeProjetoNaoExiste() {
            var request = new CriarComentarioRequest("Comentário", autorId);
            when(projetoService.buscarEntidade(projetoId))
                    .thenThrow(new EntityNotFoundException("Projeto não encontrado: " + projetoId));

            assertThatThrownBy(() -> comentarioService.criar(projetoId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Projeto não encontrado");

            verify(comentarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException se autor não existe")
        void deveLancarExcecaoSeAutorNaoExiste() {
            var request = new CriarComentarioRequest("Comentário", autorId);
            when(projetoService.buscarEntidade(projetoId)).thenReturn(projetoFixo);
            when(usuarioService.buscarPorEntidade(autorId))
                    .thenThrow(new EntityNotFoundException("Usuário não encontrado: " + autorId));

            assertThatThrownBy(() -> comentarioService.criar(projetoId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(comentarioRepository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // RF34 — Listagem de comentários por projeto
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("listarPorProjeto() — RF34")
    class ListarComentarios {

        @Test
        @DisplayName("deve retornar comentários do projeto em ordem cronológica — RF34")
        void deveListarComentariosOrdenados() {
            var c1 = Comentario.builder().conteudo("Primeiro").projeto(projetoFixo).build();
            var c2 = Comentario.builder().conteudo("Segundo").projeto(projetoFixo).build();

            when(comentarioRepository.findByProjetoIdOrderByCriadoEmAsc(projetoId))
                    .thenReturn(List.of(c1, c2));
            when(comentarioMapper.toResponse(c1)).thenReturn(
                    ComentarioResponse.builder().conteudo("Primeiro").build());
            when(comentarioMapper.toResponse(c2)).thenReturn(
                    ComentarioResponse.builder().conteudo("Segundo").build());

            List<ComentarioResponse> lista = comentarioService.listarPorProjeto(projetoId);

            assertThat(lista).hasSize(2);
            assertThat(lista.get(0).conteudo()).isEqualTo("Primeiro");
            assertThat(lista.get(1).conteudo()).isEqualTo("Segundo");
            // Deve usar a query com ordenação por criadoEm
            verify(comentarioRepository).findByProjetoIdOrderByCriadoEmAsc(projetoId);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando projeto não tem comentários")
        void deveRetornarListaVaziaSeNaoHaComentarios() {
            when(comentarioRepository.findByProjetoIdOrderByCriadoEmAsc(projetoId))
                    .thenReturn(List.of());

            List<ComentarioResponse> lista = comentarioService.listarPorProjeto(projetoId);

            assertThat(lista).isEmpty();
        }
    }
}
