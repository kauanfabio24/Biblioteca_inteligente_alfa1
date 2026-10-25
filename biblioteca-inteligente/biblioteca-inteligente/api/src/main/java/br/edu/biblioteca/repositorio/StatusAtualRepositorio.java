package br.edu.biblioteca.repositorio;

import br.edu.biblioteca.modelo.StatusAtual;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Repositório: status atual
public interface StatusAtualRepositorio extends JpaRepository<StatusAtual, Long> {

    // Busca com trava
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StatusAtual s where s.nomeLocal = :nome")
    Optional<StatusAtual> buscarComTrava(@Param("nome") String nome);

    // Listar todos por id
    List<StatusAtual> findAllByOrderByIdAsc();
}
