package com.pollen.projeto.internal;

import com.pollen.projeto.ProjetoService;
import com.pollen.projeto.internal.dto.AtualizarProjetoRequest;
import com.pollen.projeto.internal.dto.CriarProjetoRequest;
import com.pollen.projeto.internal.dto.ProjetoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Gerenciamento de projetos")
class ProjetoController {

    private final ProjetoService projetoService;

    // --- Criação e listagem via colmeia ---

    @PostMapping("/teams/{teamId}/projetos")
    @Operation(summary = "Criar projeto em uma colmeia")
    ResponseEntity<ProjetoResponse> criar(
            @PathVariable UUID teamId,
            @Valid @RequestBody CriarProjetoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetoService.criar(teamId, request));
    }

    @GetMapping("/teams/{teamId}/projetos")
    @Operation(summary = "Listar projetos de uma colmeia")
    ResponseEntity<List<ProjetoResponse>> listarPorTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(projetoService.listarPorTeam(teamId));
    }

    // --- CRUD direto por projeto ---

    @GetMapping("/projetos/{id}")
    @Operation(summary = "Consultar projeto por ID")
    ResponseEntity<ProjetoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(projetoService.buscarPorId(id));
    }

    @PutMapping("/projetos/{id}")
    @Operation(summary = "Atualizar projeto")
    ResponseEntity<ProjetoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarProjetoRequest request) {
        return ResponseEntity.ok(projetoService.atualizar(id, request));
    }

    @DeleteMapping("/projetos/{id}")
    @Operation(summary = "Excluir projeto")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        projetoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projetos/{id}/responsaveis")
    @Operation(summary = "Associar responsáveis ao projeto")
    ResponseEntity<ProjetoResponse> associarResponsaveis(
            @PathVariable UUID id,
            @RequestBody List<UUID> colaboradorIds) {
        return ResponseEntity.ok(projetoService.associarResponsaveis(id, colaboradorIds));
    }
}
