package com.pollen.usuario.internal;

import com.pollen.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Especialização de Usuario que executa atividades e participa de colmeias.
 * RN14 / RN15 — O Colaborador possui acesso limitado ao seu perfil e atividades.
 * RN22 — Um colaborador pode existir sem estar associado a nenhuma colmeia (RB06).
 *
 * Pendência D2: campo 'setor' implementado como String livre.
 * O tipo Setor não está definido na especificação.
 */
@Entity
@Table(name = "colaboradores")
@PrimaryKeyJoinColumn(name = "matricula")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Colaborador extends Usuario {

    /**
     * Setor do colaborador — String livre conforme decisão D2 do plano de ação.
     * Pendência P01: definir tipo e valores válidos.
     */
    @Column(name = "setor")
    private String setor;

    /**
     * Colmeias às quais o colaborador pertence (N:M).
     * Mapeado pelo lado dono em Team (tabela team_colaboradores).
     */
    @ManyToMany(mappedBy = "colaboradores", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.pollen.team.Team> colmeias = new ArrayList<>();

    /**
     * Projetos nos quais o colaborador é responsável (N:M).
     * Mapeado pelo lado dono em Projeto (tabela projeto_responsaveis).
     */
    @ManyToMany(mappedBy = "responsaveis", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.pollen.projeto.Projeto> projetos = new ArrayList<>();
}
