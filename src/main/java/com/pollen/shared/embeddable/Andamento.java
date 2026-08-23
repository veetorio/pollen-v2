package com.pollen.shared.embeddable;

import com.pollen.shared.enums.Status;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto embutido que representa o progresso e status de um projeto ou tarefa.
 * RN26 — O andamento deve possuir percentual de progresso e status.
 * RD10 — Projetos e tarefas devem possuir informações de progresso e status.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Andamento {

    private float progresso;

    @Enumerated(EnumType.STRING)
    private Status status;
}
