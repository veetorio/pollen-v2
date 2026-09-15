package com.pollen.config;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do GlobalExceptionHandler.
 *
 * Valida que cada tipo de exceção produz o status HTTP e o corpo
 * de resposta corretos, garantindo contratos de API previsíveis para os clientes.
 */
@DisplayName("GlobalExceptionHandler — Mapeamento de erros para HTTP")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // -----------------------------------------------------------------------
    // 404 — EntityNotFoundException
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("EntityNotFoundException → 404 Not Found")
    class EntityNotFoundTests {

        @Test
        @DisplayName("deve retornar 404 com mensagem quando recurso não é encontrado")
        void deveRetornar404ParaEntityNotFound() {
            var ex = new EntityNotFoundException("Usuário não encontrado: abc-123");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(problem.getDetail()).isEqualTo("Usuário não encontrado: abc-123");
            assertThat(problem.getTitle()).isEqualTo("Recurso não encontrado");
        }

        @Test
        @DisplayName("deve retornar 404 para colmeia não encontrada")
        void deveRetornar404ParaColmeia() {
            var ex = new EntityNotFoundException("Colmeia não encontrada: xyz");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(404);
            assertThat(problem.getDetail()).contains("Colmeia não encontrada");
        }

        @Test
        @DisplayName("deve retornar 404 para projeto não encontrado")
        void deveRetornar404ParaProjeto() {
            var ex = new EntityNotFoundException("Projeto não encontrado: xyz");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("deve retornar 404 para tarefa não encontrada")
        void deveRetornar404ParaTarefa() {
            var ex = new EntityNotFoundException("Tarefa não encontrada: xyz");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("deve retornar 404 para colaborador não encontrado")
        void deveRetornar404ParaColaborador() {
            var ex = new EntityNotFoundException("Colaborador não encontrado: xyz");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("deve retornar 404 para gestor não encontrado")
        void deveRetornar404ParaGestor() {
            var ex = new EntityNotFoundException("Gestor não encontrado: xyz");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("deve incluir timestamp na resposta")
        void deveIncluirTimestamp() {
            var ex = new EntityNotFoundException("Qualquer recurso");

            ProblemDetail problem = handler.handleEntityNotFound(ex);

            assertThat(problem.getProperties()).containsKey("timestamp");
        }
    }

    // -----------------------------------------------------------------------
    // 400 — IllegalArgumentException
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("IllegalArgumentException → 400 Bad Request")
    class IllegalArgumentTests {

        @Test
        @DisplayName("deve retornar 400 para tipo de usuário inválido — RN01")
        void deveRetornar400ParaTipoUsuarioInvalido() {
            var ex = new IllegalArgumentException("Tipo de usuário inválido: DIRETOR");

            ProblemDetail problem = handler.handleIllegalArgument(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problem.getDetail()).isEqualTo("Tipo de usuário inválido: DIRETOR");
            assertThat(problem.getTitle()).isEqualTo("Argumento inválido");
        }

        @Test
        @DisplayName("deve retornar 400 para classificação inválida de colmeia — RN18")
        void deveRetornar400ParaClassificacaoInvalida() {
            var ex = new IllegalArgumentException(
                    "No enum constant com.pollen.shared.enums.Classificacao.INVALIDA");

            ProblemDetail problem = handler.handleIllegalArgument(ex);

            assertThat(problem.getStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("deve incluir timestamp na resposta")
        void deveIncluirTimestamp() {
            var ex = new IllegalArgumentException("Erro qualquer");

            ProblemDetail problem = handler.handleIllegalArgument(ex);

            assertThat(problem.getProperties()).containsKey("timestamp");
        }
    }

    // -----------------------------------------------------------------------
    // 400 — MethodArgumentNotValidException (@Valid)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("MethodArgumentNotValidException → 400 com lista de erros")
    class ValidationTests {

        @Test
        @DisplayName("deve retornar 400 com campos inválidos quando @Valid falha")
        void deveRetornar400ComCamposInvalidos() {
            BindingResult bindingResult = mock(BindingResult.class);
            var campoNomeErro = new FieldError("request", "nome", "Nome é obrigatório");
            var campoTipoErro = new FieldError("request", "tipo", "Tipo deve ser ADMINISTRADOR, GESTOR ou COLABORADOR");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(campoNomeErro, campoTipoErro));

            var ex = new MethodArgumentNotValidException(null, bindingResult);

            ProblemDetail problem = handler.handleValidation(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problem.getTitle()).isEqualTo("Validação falhou");
            assertThat(problem.getProperties()).containsKey("erros");

            @SuppressWarnings("unchecked")
            var erros = (List<?>) problem.getProperties().get("erros");
            assertThat(erros).hasSize(2);
        }

        @Test
        @DisplayName("deve incluir campo e mensagem em cada erro de validação")
        void deveIncluirCampoEMensagem() {
            BindingResult bindingResult = mock(BindingResult.class);
            var fieldError = new FieldError("request", "senha", "Senha é obrigatória");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

            var ex = new MethodArgumentNotValidException(null, bindingResult);
            ProblemDetail problem = handler.handleValidation(ex);

            @SuppressWarnings("unchecked")
            var erros = (List<java.util.Map<String, String>>) problem.getProperties().get("erros");
            assertThat(erros.get(0)).containsEntry("campo", "senha");
            assertThat(erros.get(0)).containsEntry("mensagem", "Senha é obrigatória");
        }

        @Test
        @DisplayName("deve retornar lista vazia de erros quando não há violações")
        void deveRetornarListaVaziaSeNaoHaViolacoes() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            var ex = new MethodArgumentNotValidException(null, bindingResult);
            ProblemDetail problem = handler.handleValidation(ex);

            @SuppressWarnings("unchecked")
            var erros = (List<?>) problem.getProperties().get("erros");
            assertThat(erros).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // 400 — HttpMessageNotReadableException (JSON malformado)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("HttpMessageNotReadableException → 400 corpo inválido")
    class NotReadableTests {

        @Test
        @DisplayName("deve retornar 400 quando JSON é malformado")
        void deveRetornar400ParaJsonMalformado() {
            var inputMessage = new MockHttpInputMessage(new byte[0]);
            var ex = new HttpMessageNotReadableException("JSON malformado", inputMessage);

            ProblemDetail problem = handler.handleNotReadable(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problem.getTitle()).isEqualTo("Corpo da requisição inválido");
            assertThat(problem.getDetail()).contains("inválido ou está malformado");
        }

        @Test
        @DisplayName("deve incluir timestamp na resposta")
        void deveIncluirTimestamp() {
            var inputMessage = new MockHttpInputMessage(new byte[0]);
            var ex = new HttpMessageNotReadableException("Erro", inputMessage);

            ProblemDetail problem = handler.handleNotReadable(ex);

            assertThat(problem.getProperties()).containsKey("timestamp");
        }
    }

    // -----------------------------------------------------------------------
    // 400 — MethodArgumentTypeMismatchException (parâmetro de path inválido)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("MethodArgumentTypeMismatchException → 400 parâmetro inválido")
    class TypeMismatchTests {

        @Test
        @DisplayName("deve retornar 400 quando UUID de path é inválido")
        void deveRetornar400ParaUuidInvalido() {
            var ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("id");
            when(ex.getValue()).thenReturn("abc-nao-e-uuid");
            when(ex.getRequiredType()).thenAnswer(inv -> java.util.UUID.class);

            ProblemDetail problem = handler.handleTypeMismatch(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(problem.getTitle()).isEqualTo("Parâmetro inválido");
            assertThat(problem.getDetail())
                    .contains("id")
                    .contains("abc-nao-e-uuid")
                    .contains("UUID");
        }

        @Test
        @DisplayName("deve incluir timestamp na resposta")
        void deveIncluirTimestamp() {
            var ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("id");
            when(ex.getValue()).thenReturn("invalido");
            when(ex.getRequiredType()).thenReturn(null);

            ProblemDetail problem = handler.handleTypeMismatch(ex);

            assertThat(problem.getProperties()).containsKey("timestamp");
        }
    }

    // -----------------------------------------------------------------------
    // 500 — Fallback genérico
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Exception genérica → 500 Internal Server Error")
    class GenericErrorTests {

        @Test
        @DisplayName("deve retornar 500 sem expor mensagem interna para Exception genérica")
        void deveRetornar500ParaExcecaoGenerica() {
            var ex = new RuntimeException("Erro inesperado no banco de dados");

            ProblemDetail problem = handler.handleGeneric(ex);

            assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(problem.getTitle()).isEqualTo("Erro interno");
            // Mensagem interna NÃO deve ser exposta ao cliente
            assertThat(problem.getDetail()).doesNotContain("banco de dados");
            assertThat(problem.getDetail()).contains("erro interno");
        }

        @Test
        @DisplayName("deve incluir timestamp na resposta")
        void deveIncluirTimestamp() {
            var ex = new Exception("Qualquer coisa");

            ProblemDetail problem = handler.handleGeneric(ex);

            assertThat(problem.getProperties()).containsKey("timestamp");
        }
    }
}
