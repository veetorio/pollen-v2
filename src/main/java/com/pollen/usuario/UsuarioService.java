package com.pollen.usuario;

import com.pollen.usuario.internal.Administrador;
import com.pollen.usuario.internal.Colaborador;
import com.pollen.usuario.internal.ColaboradorRepository;
import com.pollen.usuario.internal.Gestor;
import com.pollen.usuario.internal.GestorRepository;
import com.pollen.usuario.internal.UsuarioRepository;
import com.pollen.usuario.internal.dto.AtualizarUsuarioRequest;
import com.pollen.usuario.internal.dto.CriarUsuarioRequest;
import com.pollen.usuario.internal.dto.LoginDto;
import com.pollen.usuario.internal.dto.TokenOutput;
import com.pollen.usuario.internal.dto.UsuarioResponse;
import com.pollen.usuario.internal.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * API pública do módulo usuario.
 * Exposta na raiz do pacote para que outros módulos possam depender dela.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final GestorRepository gestorRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;


    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        String senhaCriptografada = passwordEncoder.encode(request.senha());
        Usuario usuario = switch (request.tipo()) {
            case "ADMINISTRADOR" -> Administrador.builder()
                    .nome(request.nome())
                    .senha(senhaCriptografada)
                    .atividade(true)
                    .contato(request.contato() != null
                            ? usuarioMapper.toContato(request.contato())
                            : null)
                    .build();

            case "GESTOR" -> Gestor.builder()
                    .nome(request.nome())
                    .senha(senhaCriptografada)
                    .atividade(true)
                    .contato(request.contato() != null
                            ? usuarioMapper.toContato(request.contato())
                            : null)
                    .build();

            case "COLABORADOR" -> Colaborador.builder()
                    .nome(request.nome())
                    .senha(senhaCriptografada)
                    .atividade(true)
                    .setor(request.setor())
                    .contato(request.contato() != null
                            ? usuarioMapper.toContato(request.contato())
                            : null)
                    .build();

            default -> throw new IllegalArgumentException("Tipo de usuário inválido: " + request.tipo());
        };

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }
    /**
     * Cadastra um novo usuário do tipo indicado no request.
     * RF02 — Cadastro de usuários.
     */
    @Transactional
    public UsuarioResponse login(LoginDto request) {
        Usuario usuario = usuarioRepository.findByContatoEmail(request.email())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Usuário não encontrado com email: " + request.email()));
        
        if(isBcryptHash(usuario.getSenha())) {
            if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
                throw new IllegalArgumentException("Senha incorreta para o usuário com email: " + request.email());
            }
        } else {
            if (!usuario.getSenha().equals(request.senha())) {
                throw new IllegalArgumentException("Senha incorreta para o usuário com email: " + request.email());
            }
            String senhaCriptografada = passwordEncoder.encode(request.senha());
            usuario.setSenha(senhaCriptografada);
            usuarioRepository.save(usuario);
        }

        
        UsuarioResponse response = usuarioMapper.toResponse(usuario);
        TokenOutput tokenOutput = tokenService.generateToken(response);
        response = response.withAcessToken(tokenOutput);

        return response;
    }

    private boolean isBcryptHash(String senha) {
        return senha != null && senha.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
    /**
     * Lista todos os usuários cadastrados.
     * RF02 — Consulta de usuários.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    /**
     * Consulta um usuário por matrícula (UUID).
     * RF02 — Consulta de usuários.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID matricula) {
        return usuarioRepository.findById(matricula)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Usuário não encontrado: " + matricula));
    }

    /**
     * Atualiza nome, setor e contato de um usuário.
     * RF04 — Gerenciamento de perfil.
     */
    @Transactional
    public UsuarioResponse atualizar(UUID matricula, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(matricula)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Usuário não encontrado: " + matricula));

        usuario.setNome(request.nome());
        if (request.contato() != null) {
            usuario.setContato(usuarioMapper.toContato(request.contato()));
        }
        if (usuario instanceof Colaborador colaborador && request.setor() != null) {
            colaborador.setSetor(request.setor());
        }

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Exclusão física de perfil.
     * RF32 / RN06 — Somente o Administrador pode excluir fisicamente.
     * Nesta fase sem controle de acesso: qualquer chamada ao endpoint executa a exclusão.
     */
    @Transactional
    public void excluir(UUID matricula) {
        if (!usuarioRepository.existsById(matricula)) {
            throw new jakarta.persistence.EntityNotFoundException(
                    "Usuário não encontrado: " + matricula);
        }
        usuarioRepository.deleteById(matricula);
    }

    /**
     * Busca um Colaborador pelo ID — exposto para uso de outros módulos.
     */
    @Transactional(readOnly = true)
    public Colaborador buscarColaborador(UUID id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Colaborador não encontrado: " + id));
    }

    /**
     * Busca um Gestor pelo ID — exposto para uso de outros módulos.
     */
    @Transactional(readOnly = true)
    public Gestor buscarGestor(UUID id) {
        return gestorRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Gestor não encontrado: " + id));
    }

    /**
     * Busca a entidade Usuario pelo ID — exposto para uso de outros módulos.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorEntidade(UUID matricula) {
        return usuarioRepository.findById(matricula)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Usuário não encontrado: " + matricula));
    }

    /** Contagem total de usuários — exposto para o módulo admin. */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return usuarioRepository.count();
    }
}
