package com.pollen.team;

import com.pollen.projeto.Projeto;
import com.pollen.shared.enums.Classificacao;
import com.pollen.usuario.internal.Colaborador;
import com.pollen.usuario.internal.Gestor;
import jakarta.persistence.Column;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma colmeia — setor, departamento, equipe ou turma.
 * RN08 — Um Gestor pode criar uma ou mais colmeias.
 * RN18 — Toda colmeia deve possuir classificação de visibilidade.
 * RN21 — Uma colmeia pode existir sem colaboradores.
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "identificador", updatable = false, nullable = false)
    private UUID identificador;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Classificacao classificacao;

    /**
     * Gestor responsável pela colmeia — FK gestor_id em teams.
     * Decisão D (plano 2.4): Team possui FK para o Gestor criador.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id", nullable = false)
    private Gestor gestor;

    /**
     * Colaboradores da colmeia — relação N:M.
     * Tabela de junção: team_colaboradores.
     * RN22 / RB06 — Colaborador pode existir sem colmeia e vice-versa.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_colaboradores",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "colaborador_id")
    )
    @Builder.Default
    private List<Colaborador> colaboradores = new ArrayList<>();

    /**
     * Projetos vinculados à colmeia — relação 1:N.
     * RN24 — Um projeto deve estar associado a uma colmeia.
     */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Projeto> projetos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criada_em", updatable = false)
    private LocalDate criadaEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDate atualizadoEm;
}
