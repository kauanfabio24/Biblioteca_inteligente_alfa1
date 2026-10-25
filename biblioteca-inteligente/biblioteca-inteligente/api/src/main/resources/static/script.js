// Painel de lotação

// Configuração
const API_URL = "/api/status";
const INTERVALO_MS = 10000;

// Níveis
const NIVEIS = {
  VERDE:    { classe: "livre",    texto: "Livre" },
  AMARELO:  { classe: "moderado", texto: "Moderado" },
  VERMELHO: { classe: "lotado",   texto: "Lotado" }
};
const CLASSES_NIVEL = ["livre", "moderado", "lotado"];


// Funções auxiliares
const $ = (id) => document.getElementById(id);

function criar(tag, classe, texto) {
  const e = document.createElement(tag);
  if (classe) e.className = classe;
  if (texto !== undefined) e.textContent = texto;
  return e;
}

function aplicarNivel(elemento, nivel) {
  elemento.classList.remove(...CLASSES_NIVEL);
  elemento.classList.add(NIVEIS[nivel].classe);
}

function porcentagem(valor) {
  return Math.round(valor) + "%";
}

function larguraBarra(valor) {
  return Math.min(Math.max(valor, 0), 100) + "%";
}

// Data e hora
function atualizarRelogio() {
  const agora = new Date();
  const data = agora.toLocaleDateString("pt-BR", {
    weekday: "long", day: "numeric", month: "long", year: "numeric"
  });
  $("data").textContent = data.charAt(0).toUpperCase() + data.slice(1);
  const relogio = $("relogio");
  relogio.textContent = agora.toLocaleTimeString("pt-BR");
  relogio.dateTime = agora.toISOString();
}

// Lotação total
function mostrarTotal(principal, zonas) {
  $("total-ocupadas").textContent = principal.ocupacaoAtual;
  $("total-capacidade").textContent = "/ " + principal.capacidadeMaxima;
  $("total-percentual").textContent = porcentagem(principal.percentual);
  $("total-disponivel").textContent = Math.max(principal.capacidadeMaxima - principal.ocupacaoAtual, 0);
  $("zonas-lotadas").textContent = zonas.filter((z) => z.nivel === "VERMELHO").length;
  $("zonas-livres").textContent = zonas.filter((z) => z.nivel === "VERDE").length;

  const barra = $("barra-total");
  barra.style.width = larguraBarra(principal.percentual);
  aplicarNivel(barra, principal.nivel);
  aplicarNivel($("total-ocupadas"), principal.nivel);

  const selo = $("status-total");
  selo.textContent = NIVEIS[principal.nivel].texto;
  aplicarNivel(selo, principal.nivel);

  // Alerta de lotação
  $("alerta").hidden = principal.nivel !== "VERMELHO";
}

// Por piso
function mostrarPisos(zonas) {
  const lista = $("lista-pisos");
  lista.replaceChildren();

  const pisos = new Map();
  zonas.forEach((z) => {
    const p = pisos.get(z.piso) || { ocupadas: 0, capacidade: 0 };
    p.ocupadas += z.ocupacaoAtual;
    p.capacidade += z.capacidadeMaxima;
    pisos.set(z.piso, p);
  });

  if (pisos.size === 0) {
    lista.append(criar("p", "rotulo", "Sem zonas cadastradas."));
    return;
  }

  pisos.forEach((p, nome) => {
    const pct = p.capacidade ? (p.ocupadas / p.capacidade) * 100 : 0;
    const nivel = pct > 80 ? "VERMELHO" : pct >= 60 ? "AMARELO" : "VERDE";

    const item = criar("div", "piso");
    const linha = criar("div", "linha-texto");
    linha.append(criar("span", "", nome), criar("strong", "", porcentagem(pct)));

    const barra = criar("div", "barra");
    const preenchimento = criar("span");
    preenchimento.style.width = larguraBarra(pct);
    aplicarNivel(preenchimento, nivel);
    barra.append(preenchimento);

    item.append(linha, barra);
    lista.append(item);
  });
}

// Cartões das zonas
function mostrarZonas(zonas) {
  const lista = $("lista-zonas");
  lista.replaceChildren();

  if (zonas.length === 0) {
    lista.append(criar("p", "vazio", "Nenhuma zona livre cadastrada."));
    return;
  }

  zonas.forEach((z) => {
    const cartao = criar("article", "cartao zona");

    const titulo = criar("div");
    titulo.append(criar("h3", "", z.nomeExibicao), criar("p", "zona-piso", z.piso));
    const selo = criar("span", "selo", NIVEIS[z.nivel].texto);
    aplicarNivel(selo, z.nivel);
    const topo = criar("div", "linha-texto");
    topo.append(titulo, selo);

    const quantidade = criar("p", "quantidade");
    quantidade.append(criar("strong", "", z.ocupacaoAtual), criar("span", "", " / " + z.capacidadeMaxima));
    const percentual = criar("span", "percentual", porcentagem(z.percentual));
    aplicarNivel(percentual, z.nivel);
    const meio = criar("div", "linha-texto");
    meio.append(quantidade, percentual);

    const barra = criar("div", "barra");
    const preenchimento = criar("span");
    preenchimento.style.width = larguraBarra(z.percentual);
    aplicarNivel(preenchimento, z.nivel);
    barra.append(preenchimento);

    cartao.append(topo, meio, barra);
    lista.append(cartao);
  });
}

// Busca na API
let jaCarregou = false;

async function atualizar() {
  try {
    const resposta = await fetch(API_URL, { cache: "no-store" });
    if (!resposta.ok) throw new Error("HTTP " + resposta.status);
    const dados = await resposta.json();

    const principal = dados.find((d) => d.local === "PRINCIPAL");
    const zonas = dados.filter((d) => d.local !== "PRINCIPAL");

    if (principal) mostrarTotal(principal, zonas);
    mostrarPisos(zonas);
    mostrarZonas(zonas);
    jaCarregou = true;
    $("aviso-erro").hidden = true;
  } catch (erro) {
    // Tratamento de erro
    $("aviso-erro").textContent = jaCarregou
      ? "Não foi possível atualizar os dados. Exibindo a última informação recebida."
      : "Não foi possível carregar os dados. Confira se a API está rodando em " + API_URL;
    $("aviso-erro").hidden = false;
  }
}

// Inicialização
atualizarRelogio();
setInterval(atualizarRelogio, 1000);
atualizar();
setInterval(atualizar, INTERVALO_MS);
