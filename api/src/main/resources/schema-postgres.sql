-- Tabela status_atual_biblioteca
CREATE TABLE IF NOT EXISTS status_atual_biblioteca (
    id                BIGSERIAL PRIMARY KEY,
    local_nome        VARCHAR(40) NOT NULL UNIQUE,
    nome_exibicao     VARCHAR(60) NOT NULL,
    piso              VARCHAR(30) NOT NULL DEFAULT 'Geral',
    ocupacao_atual    INT NOT NULL DEFAULT 0,
    capacidade_maxima INT NOT NULL,
    atualizado_em     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela movimentacoes_log
CREATE TABLE IF NOT EXISTS movimentacoes_log (
    id             BIGSERIAL PRIMARY KEY,
    local_nome     VARCHAR(40) NOT NULL,
    tipo           VARCHAR(30) NOT NULL,
    ocupacao_apos  INT NOT NULL,
    dispositivo_id VARCHAR(60),
    registrado_em  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_log_local_data ON movimentacoes_log (local_nome, registrado_em);

-- Cadastro dos locais (não zera a ocupação ao reiniciar)
INSERT INTO status_atual_biblioteca (local_nome, nome_exibicao, piso, ocupacao_atual, capacidade_maxima) VALUES
    ('PRINCIPAL',          'Entrada principal',      'Geral',   0, 300),
    ('SALA_ESTUDOS_1',     'Sala de estudos 1',      'Térreo',  0, 30),
    ('SALA_INFORMATICA_1', 'Sala de informática 1',  'Térreo',  0, 25),
    ('SALA_LEITURA_1',     'Sala de leitura 1',      'Térreo',  0, 40),
    ('SALA_ESTUDOS_2',     'Sala de estudos 2',      '1º piso', 0, 30),
    ('SALA_INFORMATICA_2', 'Sala de informática 2',  '1º piso', 0, 25),
    ('SALA_LEITURA_2',     'Sala de leitura 2',      '1º piso', 0, 40)
ON CONFLICT (local_nome) DO UPDATE SET
    nome_exibicao     = EXCLUDED.nome_exibicao,
    piso              = EXCLUDED.piso,
    capacidade_maxima = EXCLUDED.capacidade_maxima;


-- Valores de exemplo
UPDATE status_atual_biblioteca AS s
SET ocupacao_atual = v.ocupacao, atualizado_em = CURRENT_TIMESTAMP
FROM (VALUES
    ('PRINCIPAL',          135),
    ('SALA_ESTUDOS_1',       9),
    ('SALA_INFORMATICA_1',  17),
    ('SALA_LEITURA_1',      31),
    ('SALA_ESTUDOS_2',      27),
    ('SALA_INFORMATICA_2',  24),
    ('SALA_LEITURA_2',      14)
) AS v(local, ocupacao)
WHERE s.local_nome = v.local
  AND NOT EXISTS (SELECT 1 FROM movimentacoes_log);
