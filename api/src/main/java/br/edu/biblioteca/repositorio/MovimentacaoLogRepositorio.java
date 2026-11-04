package br.edu.biblioteca.repositorio;

import br.edu.biblioteca.modelo.MovimentacaoLog;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositório: log de movimentações
public interface MovimentacaoLogRepositorio extends JpaRepository<MovimentacaoLog, Long> {
}
