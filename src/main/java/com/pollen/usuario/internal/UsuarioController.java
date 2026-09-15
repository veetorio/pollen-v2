package com.pollen.usuario.internal;

import com.pollen.usuario.UsuarioService;
import com.pollen.usuario.internal.dto.AtualizarUsuarioRequest;
import com.pollen.usuario.internal.dto.CriarUsuarioRequest;
import com.pollen.usuario.internal.dto.LoginDto;
import com.pollen.usuario.internal.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários (Administrador, Gestor, Colaborador)")
class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Cadastrar novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR')")
    @Operation(summary = "Listar todos os usuários")
    ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping("login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UsuarioResponse> login(@RequestBody LoginDto entity) {
        return ResponseEntity.ok(usuarioService.login(entity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR') or hasAuthority('COLABORADOR')")
    @Operation(summary = "Consultar usuário por ID")
    ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR') or hasAuthority('COLABORADOR')")
    @Operation(summary = "Atualizar usuário")
    ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    @Operation(summary = "Excluir fisicamente um usuário (RF32 — Administrador)")
    @ApiResponse(responseCode = "204", description = "Usuário excluído")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
