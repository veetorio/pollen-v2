package com.pollen.tarefa.internal;

import com.pollen.tarefa.TarefaService;
import com.pollen.tarefa.internal.dto.AtualizarStatusRequest;
import com.pollen.tarefa.internal.dto.AtualizarTarefaRequest;
import com.pollen.tarefa.internal.dto.CriarTarefaRequest;
import com.pollen.tarefa.internal.dto.TarefaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas e subtarefas")
class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping("/projetos/{projetoId}/tarefas")
    @Operation(summary = "Criar tarefa em um projeto")
    ResponseEntity<TarefaResponse> criar(
            @PathVariable UUID projetoId,
            @Valid @RequestBody CriarTarefaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tarefaService.criar(projetoId, request));
    }

    @GetMapping("/projetos/{projetoId}/tarefas")
    @Operation(summary = "Listar tarefas de um projeto")
    ResponseEntity<List<TarefaResponse>> listarPorProjeto(@PathVariable UUID projetoId) {
        return ResponseEntity.ok(tarefaService.listarPorProjeto(projetoId));
    }

    @GetMapping("/tarefas/{id}")
    @Operation(summary = "Consultar tarefa por ID")
    ResponseEntity<TarefaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/tarefas/{id}")
    @Operation(summary = "Atualizar tarefa")
    ResponseEntity<TarefaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarTarefaRequest request) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request));
    }

    @PatchMapping("/tarefas/{id}/status")
    @Operation(summary = "Atualizar status da tarefa (RN17)")
    ResponseEntity<TarefaResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(tarefaService.atualizarStatus(id, request));
    }

    @PostMapping("/tarefas/{id}/subtarefas")
    @Operation(summary = "Criar subtarefa (RN28)")
    ResponseEntity<TarefaResponse> criarSubtarefa(
            @PathVariable UUID id,
            @Valid @RequestBody CriarTarefaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tarefaService.criarSubtarefa(id, request));
    }

    @DeleteMapping("/tarefas/{id}")
    @Operation(summary = "Excluir tarefa")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
