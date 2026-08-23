package com.pollen.projeto.internal;

import com.pollen.projeto.ProjetoService;
import com.pollen.tarefa.TarefaConcluidaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Escuta TarefaConcluidaEvent publicado pelo módulo tarefa
 * e dispara o recálculo de progresso do projeto correspondente.
 * Fase 7 — D10 (comunicação via eventos entre módulos).
 */
@Component
@RequiredArgsConstructor
class TarefaConcluidaListener {

    private final ProjetoService projetoService;

    @ApplicationModuleListener
    public void onTarefaConcluida(TarefaConcluidaEvent event) {
        projetoService.recalcularProgresso(event.projetoId());
    }
}
