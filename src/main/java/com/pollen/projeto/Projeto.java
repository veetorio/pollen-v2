package com.pollen.projeto;

import com.pollen.shared.embeddable.Andamento;
import com.pollen.shared.embeddable.Prazo;
import com.pollen.shared.enums.Classificacao;
import com.pollen.team.Team;
import com.pollen.usuario.internal.Colaborador;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
 * Representa um projeto vinculado a uma colmeia.
 * RN24 — Um projeto deve estar associado a uma colmeia.
 * RN25 — Um projeto pode possuir um ou mais colaboradores como responsáveis.
 * RN26 — O projeto deve possuir percentual de progresso e status.
 */
@Entity
@Table(name = "projetos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Classificacao classificacao;

    /**
     * Colmeia à qual o projeto pertence — FK team_id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Colaboradores responsáveis pelo projeto — N:M.
     * Tabela de junção: projeto_responsaveis.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "projeto_responsaveis",
            joinColumns = @JoinColumn(name = "projeto_id"),
            inverseJoinColumns = @JoinColumn(name = "colaborador_id")
    )
    @Builder.Default
    private List<Colaborador> responsaveis = new ArrayList<>();

    /**
     * Tarefas do projeto — 1:N.
     * RN27 — Uma tarefa deve estar relacionada a um projeto.
     */
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.pollen.tarefa.internal.Tarefa> tarefas = new ArrayList<>();

    /**
     * Comentários do projeto — 1:N.
     * RF33 / RF34 — Comentários em projetos.
     */
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.pollen.comentario.internal.Comentario> comentarios = new ArrayList<>();

    /**
     * Prazo do projeto — objeto embutido.
     * RD08 — Projetos devem permitir representar período com início e fim.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "inicio", column = @Column(name = "prazo_inicio")),
            @AttributeOverride(name = "fim",    column = @Column(name = "prazo_fim"))
    })
    private Prazo prazo;

    /**
     * Andamento do projeto — objeto embutido.
     * RD10 — Projetos devem possuir informações de progresso e status.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "progresso", column = @Column(name = "andamento_progresso")),
            @AttributeOverride(name = "status",    column = @Column(name = "andamento_status"))
    })
    private Andamento andamento;
}
