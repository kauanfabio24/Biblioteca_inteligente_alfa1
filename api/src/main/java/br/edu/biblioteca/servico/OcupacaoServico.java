package br.edu.biblioteca.servico;

import br.edu.biblioteca.dados.ResultadoEvento;
import br.edu.biblioteca.dados.SensorEventoRequisicao;
import br.edu.biblioteca.dados.StatusResposta;
import br.edu.biblioteca.modelo.MovimentacaoLog;
import br.edu.biblioteca.modelo.StatusAtual;
import br.edu.biblioteca.modelo.TipoMovimentacao;
import br.edu.biblioteca.repositorio.MovimentacaoLogRepositorio;
import br.edu.biblioteca.repositorio.StatusAtualRepositorio;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Serviço: ocupação
@Service
public class OcupacaoServico {

    // Disparo pendente
    private record Pendente(String sensor, Instant quando) {}

    // Atributos
    private final StatusAtualRepositorio statusRepo;
    private final MovimentacaoLogRepositorio logRepo;
    private final Duration janela;
    private final int limiteAmarelo;
    private final int limiteVermelho;

    // Mapa de pendentes
    private final Map<String, Pendente> pendentes = new ConcurrentHashMap<>();

    // Construtor
    public OcupacaoServico(StatusAtualRepositorio statusRepo,
                           MovimentacaoLogRepositorio logRepo,
                           @Value("${biblioteca.janela-sequencia-segundos:4}") long janelaSegundos,
                           @Value("${biblioteca.alerta-amarelo:60}") int limiteAmarelo,
                           @Value("${biblioteca.alerta-vermelho:80}") int limiteVermelho) {
        this.statusRepo = statusRepo;
        this.logRepo = logRepo;
        this.janela = Duration.ofSeconds(janelaSegundos);
        this.limiteAmarelo = limiteAmarelo;
        this.limiteVermelho = limiteVermelho;
    }

    // Processar evento
    @Transactional
    public ResultadoEvento processar(SensorEventoRequisicao req) {
        String local = req.local().trim().toUpperCase();
        String sensor = req.sensor().trim().toUpperCase();
        String dispositivo = req.dispositivoId();

        // Busca do local
        StatusAtual status = statusRepo.buscarComTrava(local)
                .orElseThrow(() -> new LocalDesconhecidoExcecao(local));

        Instant agora = Instant.now();
        Pendente anterior = pendentes.remove(local);

        // Sequência expirada
        if (anterior != null && Duration.between(anterior.quando(), agora).compareTo(janela) > 0) {
            registrar(status, TipoMovimentacao.SEQUENCIA_EXPIRADA, dispositivo);
            anterior = null;
        }

        // Primeiro sensor
        if (anterior == null) {
            pendentes.put(local, new Pendente(sensor, agora));
            return new ResultadoEvento("AGUARDANDO_SEGUNDO_SENSOR", local, status.getOcupacaoAtual());
        }

        // Retorno ignorado
        if (anterior.sensor().equals(sensor)) {
            registrar(status, TipoMovimentacao.RETORNO_IGNORADO, dispositivo);
            return new ResultadoEvento("RETORNO_IGNORADO", local, status.getOcupacaoAtual());
        }

        // Entrada
        boolean entrada = anterior.sensor().equals("A");
        if (entrada) {
            status.setOcupacaoAtual(status.getOcupacaoAtual() + 1);
            registrar(status, TipoMovimentacao.ENTRADA, dispositivo);
            return new ResultadoEvento("ENTRADA", local, status.getOcupacaoAtual());
        }

        // Saída
        if (status.getOcupacaoAtual() == 0) {
            registrar(status, TipoMovimentacao.SAIDA_COM_LOCAL_VAZIO, dispositivo);
            return new ResultadoEvento("SAIDA_COM_LOCAL_VAZIO", local, 0);
        }
        status.setOcupacaoAtual(status.getOcupacaoAtual() - 1);
        registrar(status, TipoMovimentacao.SAIDA, dispositivo);
        return new ResultadoEvento("SAIDA", local, status.getOcupacaoAtual());
    }

    // Listar status
    @Transactional(readOnly = true)
    public List<StatusResposta> listarStatus() {
        return statusRepo.findAllByOrderByIdAsc().stream().map(this::paraResposta).toList();
    }

    // Registrar movimentação
    private void registrar(StatusAtual status, TipoMovimentacao tipo, String dispositivo) {
        status.setAtualizadoEm(LocalDateTime.now());
        logRepo.save(new MovimentacaoLog(status.getNomeLocal(), tipo, status.getOcupacaoAtual(), dispositivo));
    }

    // Converter para resposta
    private StatusResposta paraResposta(StatusAtual s) {
        double pct = s.getCapacidadeMaxima() == 0 ? 0
                : Math.round(s.getOcupacaoAtual() * 1000.0 / s.getCapacidadeMaxima()) / 10.0;
        String nivel = pct > limiteVermelho ? "VERMELHO" : pct >= limiteAmarelo ? "AMARELO" : "VERDE";
        return new StatusResposta(s.getNomeLocal(), s.getNomeExibicao(), s.getPiso(),
                s.getOcupacaoAtual(), s.getCapacidadeMaxima(), pct, nivel, s.getAtualizadoEm());
    }
}
