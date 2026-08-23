package com.pollen.shared.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto embutido com informações de contato do usuário.
 * Mapeado diretamente nas colunas da tabela usuarios.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contato {

    @Email
    @Column(unique = true)
    private String email;

    @Email
    @Column(unique = true)
    private String emailSecundario;

    @Column(unique = true)
    private String telefone;

    @Column(unique = true)  
    private String telefoneSecundario;
}
