package com.pollen.team.internal;

import com.pollen.team.TeamService;
import com.pollen.team.internal.dto.AtualizarTeamRequest;
import com.pollen.team.internal.dto.CriarTeamRequest;
import com.pollen.team.internal.dto.TeamResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Colmeias", description = "Gerenciamento de colmeias (Teams)")
class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Criar colmeia")
    ResponseEntity<TeamResponse> criar(@Valid @RequestBody CriarTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as colmeias")
    ResponseEntity<List<TeamResponse>> listar() {
        return ResponseEntity.ok(teamService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar colmeia por ID")
    ResponseEntity<TeamResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar colmeia")
    ResponseEntity<TeamResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarTeamRequest request) {
        return ResponseEntity.ok(teamService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir colmeia")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        teamService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    @Operation(summary = "Adicionar colaborador à colmeia")
    ResponseEntity<TeamResponse> adicionarColaborador(
            @RequestParam UUID teamId,
            @RequestParam UUID colaboradorId) {
        return ResponseEntity.ok(teamService.adicionarColaborador(teamId, colaboradorId));
    }

    @DeleteMapping("/{id}/colaboradores/{colaboradorId}")
    @Operation(summary = "Remover colaborador da colmeia")
    ResponseEntity<TeamResponse> removerColaborador(
            @PathVariable UUID id,
            @PathVariable UUID colaboradorId) {
        return ResponseEntity.ok(teamService.removerColaborador(id, colaboradorId));
    }
}
