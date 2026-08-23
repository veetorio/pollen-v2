package com.pollen.usuario;

import com.pollen.shared.embeddable.Contato;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade base da hierarquia de usuários.
 * RO1 / RN02 — Usuario é a superclasse do domínio.
 * Estratégia JOINED: tabela usuarios + tabelas específicas por subtipo.
 */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "matricula", updatable = false, nullable = false)
    private UUID matricula;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private Boolean atividade;

    @Embedded
    private Contato contato;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDate criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDate atualizadoEm;
}
