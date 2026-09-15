package com.pollen.comentario.internal;

import com.pollen.comentario.ComentarioService;
import com.pollen.comentario.internal.dto.ComentarioResponse;
import com.pollen.comentario.internal.dto.CriarComentarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projetos/{projetoId}/comentarios")
@RequiredArgsConstructor
@Tag(name = "Comentários", description = "Comentários associados a projetos")
class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adicionar comentário ao projeto (RF33)")
    ResponseEntity<ComentarioResponse> criar(
            @PathVariable UUID projetoId,
            @Valid @RequestBody CriarComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioService.criar(projetoId, request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar comentários do projeto (RF34)")
    ResponseEntity<List<ComentarioResponse>> listar(@PathVariable UUID projetoId) {
        return ResponseEntity.ok(comentarioService.listarPorProjeto(projetoId));
    }
}
