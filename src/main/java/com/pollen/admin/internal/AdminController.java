package com.pollen.admin.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller do módulo admin.
 * RF29 / RF30 — Dashboard e operações administrativas.
 * RN05 — Somente o Administrador pode acessar o dashboard.
 * Autorização por perfil será implementada na fase de segurança (Spring Security).
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administração", description = "Dashboard e operações administrativas (Administrador)")
class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard administrativo (RF29 — somente Administrador)")
    @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados")
    ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }
}
