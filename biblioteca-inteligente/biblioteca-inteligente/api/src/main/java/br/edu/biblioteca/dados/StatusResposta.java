package br.edu.biblioteca.dados;

import java.time.LocalDateTime;

// Dados: resposta do status
public record StatusResposta(
        String local,
        String nomeExibicao,
        String piso,
        int ocupacaoAtual,
        int capacidadeMaxima,
        double percentual,
        String nivel,
        LocalDateTime atualizadoEm) {
}
