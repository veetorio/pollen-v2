package com.pollen.comentario;

import com.pollen.comentario.internal.Comentario;
import com.pollen.comentario.internal.ComentarioRepository;
import com.pollen.comentario.internal.dto.ComentarioResponse;
import com.pollen.comentario.internal.dto.CriarComentarioRequest;
import com.pollen.comentario.internal.mapper.ComentarioMapper;
import com.pollen.projeto.Projeto;
import com.pollen.projeto.ProjetoService;
import com.pollen.usuario.Usuario;
import com.pollen.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * API pública do módulo comentario.
 */
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ComentarioMapper comentarioMapper;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    /**
     * Adiciona um comentário a um projeto.
     * RF33 — Adicionar comentário em projetos.
     */
    @Transactional
    public ComentarioResponse criar(UUID projetoId, CriarComentarioRequest request) {
        Projeto projeto = projetoService.buscarEntidade(projetoId);

        Usuario autor = null;
        if (request.autorId() != null) {
            autor = usuarioService.buscarPorEntidade(request.autorId());
        }

        Comentario comentario = Comentario.builder()
                .conteudo(request.conteudo())
                .projeto(projeto)
                .autor(autor)
                .build();

        return comentarioMapper.toResponse(comentarioRepository.save(comentario));
    }

    /**
     * Lista comentários de um projeto em ordem cronológica.
     * RF34 — Consultar comentários.
     */
    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorProjeto(UUID projetoId) {
        return comentarioRepository.findByProjetoIdOrderByCriadoEmAsc(projetoId)
                .stream()
                .map(comentarioMapper::toResponse)
                .toList();
    }
}
