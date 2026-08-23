package com.pollen.tarefa.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {

    /** Tarefas raiz de um projeto (sem pai). */
    List<Tarefa> findByProjetoIdAndTarefaPaiIsNull(UUID projetoId);
}
