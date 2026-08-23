package com.pollen.tarefa.internal;

import com.pollen.projeto.Projeto;
import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.embeddable.Prazo;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma tarefa ou subtarefa de um projeto.
 * RN27 — Uma tarefa deve estar relacionada a um projeto.
 * RN28 / RB05 — Uma tarefa pode possuir zero ou mais subtarefas (auto-referência).
 * RD05 — Tarefas hierárquicas: tarefa pai → subtarefas filhas.
 */
@Entity
@Table(name = "tarefas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    /**
     * Projeto ao qual a tarefa pertence — FK projeto_id.
     * Mesmo subtarefas mantêm FK para o projeto raiz para facilitar consultas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    /**
     * Tarefa pai — auto-referência.
     * Nullable: tarefas raiz não possuem pai.
     * FK tarefa_pai_id em tarefas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_pai_id")
    private Tarefa tarefaPai;

    /**
     * Subtarefas desta tarefa — auto-referência 1:N.
     */
    @OneToMany(mappedBy = "tarefaPai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Tarefa> subtarefas = new ArrayList<>();

    /**
     * Prazo da tarefa — objeto embutido.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "inicio", column = @Column(name = "prazo_inicio")),
            @AttributeOverride(name = "fim",    column = @Column(name = "prazo_fim"))
    })
    private Prazo prazo;

    /**
     * Andamento da tarefa — objeto embutido.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "progresso", column = @Column(name = "andamento_progresso")),
            @AttributeOverride(name = "status",    column = @Column(name = "andamento_status"))
    })
    private Andamento andamento;
}
