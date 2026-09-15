package com.pollen.config;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global de exceções da API Pollen.
 *
 * Garante respostas HTTP padronizadas (RFC 9457 — Problem Details) para todos
 * os erros previsíveis levantados pelos services e controllers.
 *
 * Exceções tratadas:
 *
 * 404 — EntityNotFoundException
 *   Lançada pelos services quando um recurso não é encontrado pelo ID.
 *   Exemplos: usuário, colmeia, projeto, tarefa, colaborador, gestor.
 *
 * 400 — IllegalArgumentException
 *   Lançada pelos services quando um argumento de negócio é inválido.
 *   Exemplos: tipo de usuário inválido (RN01), classificação inválida (RN18),
 *   status de tarefa inválido (RN17), enum não reconhecido pelo valueOf().
 *
 * 400 — MethodArgumentNotValidException
 *   Lançada pelo Spring MVC quando a validação de @Valid falha em um DTO.
 *   Retorna a lista de campos inválidos com as mensagens de validação.
 *   Exemplos: nome em branco, tipo obrigatório ausente, email inválido.
 *
 * 400 — HttpMessageNotReadableException
 *   Lançada quando o corpo da requisição não pode ser desserializado.
 *   Exemplo: JSON malformado, campo esperado com tipo errado.
 *
 * 400 — MethodArgumentTypeMismatchException
 *   Lançada quando um parâmetro de path ou query não pode ser convertido.
 *   Exemplo: UUID inválido em /usuarios/{id}.
 *
 * 500 — Exception (fallback)
 *   Captura qualquer exceção não prevista para evitar stack traces na resposta.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TIPO_NAO_ENCONTRADO =
            URI.create("https://pollen.dev/erros/nao-encontrado");
    private static final URI TIPO_REQUISICAO_INVALIDA =
            URI.create("https://pollen.dev/erros/requisicao-invalida");
    private static final URI TIPO_ERRO_INTERNO =
            URI.create("https://pollen.dev/erros/erro-interno");

    // -----------------------------------------------------------------------
    // 404 — Recurso não encontrado
    // -----------------------------------------------------------------------

    /**
     * EntityNotFoundException → 404 Not Found.
     *
     * Disparado por todos os services quando buscarPorId / buscarEntidade / etc.
     * não localizam o recurso no repositório.
     *
     * Casos cobertos nos testes:
     * - UsuarioServiceTest: buscarPorId com UUID inexistente
     * - UsuarioServiceTest: buscarColaborador com UUID inexistente
     * - UsuarioServiceTest: buscarGestor com UUID inexistente
     * - TeamServiceTest: buscarPorId com UUID inexistente
     * - TeamServiceTest: adicionarColaborador com team/colaborador inexistente
     * - ProjetoServiceTest: buscarPorId com UUID inexistente
     * - TarefaServiceTest: buscarPorId com UUID inexistente
     * - TarefaServiceTest: criarSubtarefa com tarefaPai inexistente
     * - ComentarioServiceTest: criar com projeto inexistente
     * - ComentarioServiceTest: criar com autor inexistente
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso não encontrado");
        problem.setType(TIPO_NAO_ENCONTRADO);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // -----------------------------------------------------------------------
    // 400 — Argumento de negócio inválido
    // -----------------------------------------------------------------------

    /**
     * IllegalArgumentException → 400 Bad Request.
     *
     * Disparado pelos services e pelo Java Enum.valueOf() quando um valor
     * de negócio não é reconhecido.
     *
     * Casos cobertos nos testes:
     * - UsuarioServiceTest: criar com tipo="DIRETOR" (tipo inválido — RN01)
     * - TeamServiceTest: criar com classificacao="INVALIDA" (enum inválido — RN18)
     * - ProjetoServiceTest: criar com classificacao="ERRADA" (enum inválido)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Argumento inválido");
        problem.setType(TIPO_REQUISICAO_INVALIDA);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // -----------------------------------------------------------------------
    // 400 — Validação de DTO (@Valid)
    // -----------------------------------------------------------------------

    /**
     * MethodArgumentNotValidException → 400 Bad Request com lista de campos.
     *
     * Disparado pelo Spring MVC quando a anotação @Valid rejeita o DTO recebido.
     * Retorna um objeto "erros" com os campos e suas mensagens de validação.
     *
     * Campos validados nos DTOs (baseado nos testes de caso negativo):
     * - CriarUsuarioRequest: nome @NotBlank, senha @NotBlank, tipo @NotNull @Pattern
     * - CriarTeamRequest: nome @NotBlank, classificacao @NotNull, gestorId @NotNull
     * - CriarProjetoRequest: nome @NotBlank, classificacao @NotNull
     * - CriarTarefaRequest: titulo @NotBlank
     * - AtualizarStatusRequest: status @NotBlank @Pattern(PENDENTE|PROGREDINDO|CONCLUIDO)
     * - ContatoDTO: email @Email, emailSecundario @Email
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toErrorMap)
                .collect(Collectors.toList());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisição são inválidos.");
        problem.setTitle("Validação falhou");
        problem.setType(TIPO_REQUISICAO_INVALIDA);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("erros", erros);
        return problem;
    }

    // -----------------------------------------------------------------------
    // 400 — JSON malformado ou tipo incompatível no body
    // -----------------------------------------------------------------------

    /**
     * HttpMessageNotReadableException → 400 Bad Request.
     *
     * Disparado quando o Spring não consegue desserializar o corpo da requisição.
     *
     * Casos de erro esperados:
     * - Body ausente em endpoints que exigem @RequestBody
     * - JSON com syntax error (chave sem aspas, vírgula extra)
     * - Valor de tipo incompatível (string onde se espera número)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição é inválido ou está malformado.");
        problem.setTitle("Corpo da requisição inválido");
        problem.setType(TIPO_REQUISICAO_INVALIDA);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // -----------------------------------------------------------------------
    // 400 — Parâmetro de path/query com tipo incorreto
    // -----------------------------------------------------------------------

    /**
     * MethodArgumentTypeMismatchException → 400 Bad Request.
     *
     * Disparado quando um @PathVariable ou @RequestParam não pode ser convertido
     * para o tipo declarado no método do controller.
     *
     * Casos de erro esperados nos testes:
     * - GET /usuarios/{id} com id="abc" (não é um UUID válido)
     * - GET /teams/{id} com id="123" (não é um UUID válido)
     * - PATCH /tarefas/{id}/status com id inválido
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detalhe = String.format(
                "O parâmetro '%s' recebeu o valor '%s', que não é do tipo esperado '%s'.",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, detalhe);
        problem.setTitle("Parâmetro inválido");
        problem.setType(TIPO_REQUISICAO_INVALIDA);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // -----------------------------------------------------------------------
    // 500 — Fallback para erros não previstos
    // -----------------------------------------------------------------------

    /**
     * Exception genérica → 500 Internal Server Error.
     *
     * Captura qualquer exceção não tratada pelos handlers específicos.
     * Evita que stack traces sejam expostos na resposta.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Por favor, tente novamente mais tarde.");
        problem.setTitle("Erro interno");
        problem.setType(TIPO_ERRO_INTERNO);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // -----------------------------------------------------------------------
    // Utilitários privados
    // -----------------------------------------------------------------------

    private Map<String, String> toErrorMap(FieldError error) {
        return Map.of(
                "campo", error.getField(),
                "mensagem", error.getDefaultMessage() != null
                        ? error.getDefaultMessage()
                        : "Valor inválido"
        );
    }
}
