package com.pollen.tarefa;

import java.util.UUID;

/**
 * Evento publicado quando uma tarefa tem seu status alterado para CONCLUIDO.
 * O módulo projeto escuta este evento para recalcular o progresso.
 * Fase 7 do plano de ação — D10.
 */
public record TarefaConcluidaEvent(UUID tarefaId, UUID projetoId) {}
