package com.pollen.admin.internal;

import com.pollen.projeto.ProjetoService;
import com.pollen.tarefa.TarefaService;
import com.pollen.team.TeamService;
import com.pollen.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço do módulo admin.
 * RF29 / RF30 — Dashboard e operações administrativas.
 * Usa APIs públicas dos módulos para não cruzar fronteiras internas.
 * Pendência P04: Empresa não modelada — dashboard retorna contagens básicas.
 */
@Service
@RequiredArgsConstructor
class AdminService {

    private final UsuarioService usuarioService;
    private final TeamService teamService;
    private final ProjetoService projetoService;
    private final TarefaService tarefaService;

    @Transactional(readOnly = true)
    AdminDashboardResponse dashboard() {
        return AdminDashboardResponse.builder()
                .totalUsuarios(usuarioService.contarTodos())
                .totalColmeias(teamService.contarTodos())
                .totalProjetos(projetoService.contarTodos())
                .totalTarefas(tarefaService.contarTodos())
                .build();
    }
}
