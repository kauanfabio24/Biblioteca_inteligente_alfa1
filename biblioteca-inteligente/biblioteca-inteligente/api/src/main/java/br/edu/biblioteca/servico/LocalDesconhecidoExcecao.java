package br.edu.biblioteca.servico;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Exceção: local desconhecido
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LocalDesconhecidoExcecao extends RuntimeException {

    // Construtor
    public LocalDesconhecidoExcecao(String local) {
        super("Local desconhecido: " + local);
    }
}
