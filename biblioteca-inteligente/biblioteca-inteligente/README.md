# Como usar

Projeto que mostra a lotação da biblioteca em tempo real. O ESP32 (simulado no Wokwi) manda os disparos dos sensores para uma API em Java, que guarda tudo no MySQL e mostra num painel web.

A API já entrega o painel, então tudo roda em um endereço só: `http://localhost:8080`.

## O que precisa ter instalado

- Java 17
- Maven
- MySQL (com o MySQL Workbench)
- Uma conta no Wokwi
- Um túnel para o Wokwi enxergar seu PC (cloudflared ou ngrok)

## Passo a passo

### 1. Banco de dados

Abra o MySQL Workbench, conecte no seu servidor, abra o arquivo `banco/schema.sql` e execute tudo. Isso cria o banco `biblioteca`, as tabelas e os locais (entrada principal e as seis salas).

### 2. API

Num terminal (PowerShell), dentro da pasta `api`:

```
$env:DB_USER="root"
$env:DB_PASSWORD="sua_senha_do_mysql"
mvn spring-boot:run
```

Espere aparecer `Started AplicacaoBiblioteca` e deixe o terminal aberto.

### 3. Painel

Abra `http://localhost:8080` no navegador. Ele atualiza sozinho a cada 10 segundos.

### 4. Testar sem o ESP32

Mande dois disparos seguidos (sensor A e depois B, em até 4 segundos) para contar uma entrada:

```
curl.exe -X POST http://localhost:8080/api/sensores/evento -H "Content-Type: application/json" -d "{\"dispositivoId\":\"teste\",\"local\":\"PRINCIPAL\",\"sensor\":\"A\"}"
curl.exe -X POST http://localhost:8080/api/sensores/evento -H "Content-Type: application/json" -d "{\"dispositivoId\":\"teste\",\"local\":\"PRINCIPAL\",\"sensor\":\"B\"}"
```

Em até 10 segundos o painel mostra 1 pessoa. Para testar uma saída, mande B e depois A.

### 5. Usar o ESP32 no Wokwi

O Wokwi roda na nuvem e não enxerga o `localhost` do seu PC, então precisa de um túnel:

1. Com a API rodando, abra outro terminal e rode um dos comandos:
   - `cloudflared tunnel --url http://localhost:8080`
   - `ngrok http 8080`
2. Copie a URL pública que aparecer.
3. No Wokwi, crie um projeto ESP32 e cole o conteúdo de `esp32-wokwi/sketch.ino` e `esp32-wokwi/diagram.json`.
4. No `sketch.ino`, troque o `API_URL` pela sua URL, mantendo o final `/api/sensores/evento`. Exemplo:
   ```
   https://sua-url-publica/api/sensores/evento
   ```
5. Rode a simulação.

A URL do túnel muda toda vez que você reinicia ele, então lembre de atualizar no Wokwi.

## Botões do Wokwi

- **Principal A / B:** sensores da entrada principal (A fora, B dentro).
- **Sala A / B:** sensores da sala que está selecionada.
- **Trocar sala (amarelo):** passa para a próxima sala. O Monitor Serial mostra qual está selecionada.

Para entrar, aperte A e depois B. Para sair, aperte B e depois A.

## Como a contagem funciona

- A e depois B em até 20 segundos (por conta do delay do sensor online "wokwi", mas normalmente seria 4 segundos), conta como entrada (+1).
- B e depois A em até 20 segundos (por conta do delay do sensor online "wokwi", mas normalmente seria 4 segundos), conta como saída (-1).
- O mesmo sensor duas vezes seguidas é descartado (a pessoa voltou no meio do caminho).
- Se o segundo sensor não disparar em 20 segundos (por conta do delay do sensor online "wokwi", mas normalmente seria 4 segundos), nada é contado.
- A contagem nunca fica negativa.
- No painel: livre abaixo de 60%, moderado de 60% a 80% e lotado acima de 80%. Esses valores ficam em `api/src/main/resources/application.properties`.
- Para criar um local novo, é só inserir uma linha na tabela `status_atual_biblioteca`. O painel mostra sozinho.

## Rotas da API

| Método | Rota | Para que serve |
|---|---|---|
| POST | `/api/sensores/evento` | O ESP32 envia `{dispositivoId, local, sensor}` |
| GET | `/api/status` | O painel lê a ocupação de cada local |

## Acessar pelo celular

Se o celular estiver no mesmo Wi-Fi do PC, abra `http://IP-DO-PC:8080` (veja o IP com `ipconfig`). Fora da sua rede, use a URL pública do túnel.
