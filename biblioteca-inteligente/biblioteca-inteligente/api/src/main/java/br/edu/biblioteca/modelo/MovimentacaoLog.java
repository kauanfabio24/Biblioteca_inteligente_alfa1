package br.edu.biblioteca.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidade: log de movimentações
@Entity
@Table(name = "movimentacoes_log")
public class MovimentacaoLog {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_nome", nullable = false, length = 40)
    private String nomeLocal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Column(name = "ocupacao_apos", nullable = false)
    private int ocupacaoApos;

    @Column(name = "dispositivo_id", length = 60)
    private String dispositivoId;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    // Construtores
    protected MovimentacaoLog() {}

    public MovimentacaoLog(String nomeLocal, TipoMovimentacao tipo, int ocupacaoApos, String dispositivoId) {
        this.nomeLocal = nomeLocal;
        this.tipo = tipo;
        this.ocupacaoApos = ocupacaoApos;
        this.dispositivoId = dispositivoId;
        this.registradoEm = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getNomeLocal() { return nomeLocal; }
    public TipoMovimentacao getTipo() { return tipo; }
    public int getOcupacaoApos() { return ocupacaoApos; }
    public String getDispositivoId() { return dispositivoId; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
}
