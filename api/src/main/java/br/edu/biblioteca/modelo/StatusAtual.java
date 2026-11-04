package br.edu.biblioteca.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidade: status atual dos locais
@Entity
@Table(name = "status_atual_biblioteca")
public class StatusAtual {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_nome", nullable = false, unique = true, length = 40)
    private String nomeLocal;

    @Column(name = "nome_exibicao", nullable = false, length = 60)
    private String nomeExibicao;

    @Column(nullable = false, length = 30)
    private String piso;

    @Column(name = "ocupacao_atual", nullable = false)
    private int ocupacaoAtual;

    @Column(name = "capacidade_maxima", nullable = false)
    private int capacidadeMaxima;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    // Getters e setters
    public Long getId() { return id; }
    public String getNomeLocal() { return nomeLocal; }
    public String getNomeExibicao() { return nomeExibicao; }
    public String getPiso() { return piso; }
    public int getOcupacaoAtual() { return ocupacaoAtual; }
    public void setOcupacaoAtual(int ocupacaoAtual) { this.ocupacaoAtual = ocupacaoAtual; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
