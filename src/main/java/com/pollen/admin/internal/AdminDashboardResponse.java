package com.pollen.admin.internal;

import lombok.Builder;

/**
 * Resposta do dashboard administrativo.
 * RF29 / RN05 — Somente o Administrador acessa o dashboard.
 * Pendência P04: entidade Empresa não especificada — contagens básicas por ora.
 */
@Builder
record AdminDashboardResponse(
        long totalUsuarios,
        long totalColmeias,
        long totalProjetos,
        long totalTarefas
) {}
