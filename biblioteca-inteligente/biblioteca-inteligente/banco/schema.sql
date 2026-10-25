-- Banco de dados
CREATE DATABASE IF NOT EXISTS biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca;

-- Tabela status_atual_biblioteca
CREATE TABLE IF NOT EXISTS status_atual_biblioteca (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    local_nome        VARCHAR(40) NOT NULL UNIQUE,
    nome_exibicao     VARCHAR(60) NOT NULL,
    piso              VARCHAR(30) NOT NULL DEFAULT 'Geral',
    ocupacao_atual    INT NOT NULL DEFAULT 0,
    capacidade_maxima INT NOT NULL,
    atualizado_em     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

-- Tabela movimentacoes_log
CREATE TABLE IF NOT EXISTS movimentacoes_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    local_nome     VARCHAR(40) NOT NULL,
    tipo           VARCHAR(30) NOT NULL,
    ocupacao_apos  INT NOT NULL,
    dispositivo_id VARCHAR(60),
    registrado_em  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_log_local_data (local_nome, registrado_em)
);

-- Limpeza de zonas antigas
DELETE FROM status_atual_biblioteca WHERE local_nome LIKE 'ZONA\_LIVRE\_%';

-- Cadastro dos locais
INSERT INTO status_atual_biblioteca (local_nome, nome_exibicao, piso, ocupacao_atual, capacidade_maxima) VALUES
    ('PRINCIPAL',          'Entrada principal',   'Geral',   0, 300),
    ('SALA_ESTUDOS_1',     'Sala de estudos 1',   'Térreo',  0, 30),
    ('SALA_INFORMATICA_1', 'Sala de informática 1','Térreo', 0, 25),
    ('SALA_LEITURA_1',     'Sala de leitura 1',   'Térreo',  0, 40),
    ('SALA_ESTUDOS_2',     'Sala de estudos 2',   '1º piso', 0, 30),
    ('SALA_INFORMATICA_2', 'Sala de informática 2','1º piso',0, 25),
    ('SALA_LEITURA_2',     'Sala de leitura 2',   '1º piso', 0, 40)
ON DUPLICATE KEY UPDATE
    nome_exibicao = VALUES(nome_exibicao),
    piso = VALUES(piso),
    capacidade_maxima = VALUES(capacidade_maxima);
