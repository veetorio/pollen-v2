package com.pollen.shared.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Objeto embutido que representa o período de execução de um projeto ou tarefa.
 * RD08 — Projetos e tarefas devem permitir representar um período com início e fim.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prazo {

    private LocalDateTime inicio;

    private LocalDateTime fim;
}
