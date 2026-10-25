package br.edu.biblioteca.dados;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dados: requisição do evento do sensor
public record SensorEventoRequisicao(
        @NotBlank @Size(max = 60) String dispositivoId,
        @NotBlank @Size(max = 40) String local,
        @NotBlank @Pattern(regexp = "(?i)[AB]", message = "sensor deve ser A ou B") String sensor) {
}
