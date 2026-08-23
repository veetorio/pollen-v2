package com.pollen.usuario.internal;

import com.pollen.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Especialização de Usuario com privilégios administrativos máximos.
 * RN03 / RN04 — O Administrador possui acesso total à plataforma.
 * RN06 — Somente o Administrador pode excluir fisicamente perfis de usuário.
 */
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "matricula")
@NoArgsConstructor
@SuperBuilder
public class Administrador extends Usuario {
}
