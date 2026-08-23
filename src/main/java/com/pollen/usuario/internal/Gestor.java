package com.pollen.usuario.internal;

import com.pollen.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
 * Especialização de Usuario responsável por criar e administrar colmeias.
 * RN07 / RN08 — O Gestor é responsável por suas colmeias.
 * A lista de times é carregada via LAZY para evitar N+1.
 * A referência a Team é feita pelo nome qualificado para não cruzar fronteiras de módulo internamente.
 */
@Entity
@Table(name = "gestores")
@PrimaryKeyJoinColumn(name = "matricula")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Gestor extends Usuario {

    /**
     * Relacionamento Gestor → Team (1:N).
     * Mapeado pelo lado dono em Team (gestor_id).
     * Referência pelo nome da classe para não importar pacote interno de outro módulo.
     */
    @OneToMany(mappedBy = "gestor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.pollen.team.Team> times = new ArrayList<>();
}
