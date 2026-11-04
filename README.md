# Biblioteca Inteligente

Projeto que mostra a lotação da biblioteca em tempo real. O ESP32 (simulado no Wokwi) manda os disparos dos sensores para uma API, que guarda tudo num banco de dados e mostra num painel web.

Este guia explica como colocar o projeto no ar usando o **Render**.

## O que você precisa

- Uma conta no [GitHub](https://github.com)
- Uma conta no [Render](https://render.com)
- Uma conta no [Wokwi](https://wokwi.com)

## Passo a passo

### 1. Coloque o projeto no GitHub

Crie um repositório no GitHub e envie o projeto para lá. As pastas `api`, `banco` e `esp32-wokwi` e o arquivo `README.md` devem ficar na primeira página do repositório, e não dentro de outra pasta.

### 2. Crie o banco de dados

1. No Render, clique em **New** e escolha **PostgreSQL**.
2. Dê um nome ao banco e crie (pode ser o plano gratuito).
3. Quando ficar pronto, abra o banco e anote: **Hostname**, **Database**, **Username** e **Password**.
4. Monte o endereço do banco assim, trocando as partes em maiúsculas pelos dados que você anotou:

```
jdbc:postgresql://HOSTNAME:5432/DATABASE
```

### 3. Crie a API

1. No Render, clique em **New** e escolha **Web Service**.
2. Selecione o repositório do projeto.
3. Em **Language**, escolha **Docker**.
4. Em **Root Directory**, escreva `api`.
5. Em **Environment Variables**, adicione estas:

| Nome | O que colocar |
|---|---|
| `DB_URL` | o endereço que você montou no passo 2 |
| `DB_USER` | o Username do banco |
| `DB_PASSWORD` | a Password do banco |
| `SQL_INIT` | `always` |
| `JANELA_SEQUENCIA_SEGUNDOS` | `25` |

6. Clique em **Create Web Service** e espere terminar de construir. Pode levar alguns minutos.

O `SQL_INIT` com o valor `always` faz a API criar as tabelas e as salas sozinha. Você não precisa rodar nenhum script no banco.

### 4. Veja se funcionou

Quando terminar, o Render mostra o endereço do seu site, parecido com:

```
https://nome-do-app.onrender.com
```

Abra esse endereço no navegador. Se o painel aparecer com a entrada principal e as seis salas zeradas, está funcionando.

### 5. Ligue o Wokwi

1. Acesse o Wokwi, clique em **New Project** e escolha **ESP32**.
2. Cole o conteúdo de `esp32-wokwi/diagram.json` na aba `diagram.json`.
3. Cole o conteúdo de `esp32-wokwi/sketch.ino` na aba `sketch.ino`.
4. No `sketch.ino`, na linha do `API_URL`, coloque o endereço do seu app no Render, com `/api/sensores/evento` no final:

```
https://nome-do-app.onrender.com/api/sensores/evento
```

5. Clique no botão verde **Start the simulation**.
6. No Monitor Serial, espere aparecer `Conectando ao Wi-Fi... ok` e depois `Pronto`.

## Como testar

**Antes de testar, abra o site do Render no navegador e espere o painel carregar.** No plano gratuito, o Render dorme depois de um tempo sem uso, e essa visita acorda a API.

- **Entrada (+1):** clique em **Principal A** e, em até 25 segundos, em **Principal B**.
- **Saída (-1):** clique em **Principal B** e, em até 25 segundos, em **Principal A**.
- **Salas:** clique no botão amarelo **Trocar sala** para escolher a sala (o Monitor Serial mostra qual está selecionada) e use **Sala A** e **Sala B** do mesmo jeito.

O painel atualiza sozinho a cada 10 segundos.

## Regras da contagem

- A e depois B, em até 25 segundos, conta como entrada (+1).
- B e depois A, em até 25 segundos, conta como saída (-1).
- O mesmo sensor duas vezes seguidas é descartado.
- Se o segundo sensor não disparar a tempo, nada é contado.
- A contagem nunca fica negativa.
- No painel: livre abaixo de 60%, moderado de 60% a 80% e lotado acima de 80%.

## Atualizar o projeto

Sempre que tiver uma mudança no GitHub, o Render atualiza a API sozinho.
