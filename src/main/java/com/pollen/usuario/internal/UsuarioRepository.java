package com.pollen.usuario.internal;

import com.pollen.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByContatoEmail(String email);

    Optional<Usuario> findByContatoEmail(String email);
}
