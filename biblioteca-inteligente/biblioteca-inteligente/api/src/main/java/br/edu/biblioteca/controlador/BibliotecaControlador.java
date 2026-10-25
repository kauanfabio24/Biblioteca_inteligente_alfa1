package br.edu.biblioteca.controlador;

import br.edu.biblioteca.dados.ResultadoEvento;
import br.edu.biblioteca.dados.SensorEventoRequisicao;
import br.edu.biblioteca.dados.StatusResposta;
import br.edu.biblioteca.servico.OcupacaoServico;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

// Controlador: biblioteca
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BibliotecaControlador {

    // Serviço
    private final OcupacaoServico service;

    // Construtor
    public BibliotecaControlador(OcupacaoServico service) {
        this.service = service;
    }

    // Rota: receber evento
    @PostMapping("/sensores/evento")
    public ResultadoEvento receberEvento(@Valid @RequestBody SensorEventoRequisicao req) {
        return service.processar(req);
    }

    // Rota: status
    @GetMapping("/status")
    public List<StatusResposta> status() {
        return service.listarStatus();
    }
}
