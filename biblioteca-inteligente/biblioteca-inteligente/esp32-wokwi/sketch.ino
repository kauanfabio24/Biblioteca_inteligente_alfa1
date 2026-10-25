// Bibliotecas
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>

// Configurações
const char* WIFI_SSID = "Wokwi-GUEST";
const char* WIFI_PASS = "";
const char* API_URL   = "https://SEU-TUNEL.trycloudflare.com/api/sensores/evento";
const char* DEVICE_ID = "esp32-biblioteca-01";

// Salas
const char* SALAS[] = {
  "SALA_ESTUDOS_1", "SALA_ESTUDOS_2",
  "SALA_INFORMATICA_1", "SALA_INFORMATICA_2",
  "SALA_LEITURA_1", "SALA_LEITURA_2"
};
const int TOTAL_SALAS = sizeof(SALAS) / sizeof(SALAS[0]);
int salaAtual = 0;

// Tempo anti-repique
const unsigned long DEBOUNCE_MS = 250;

// Tipos de botão
enum Tipo { PRINCIPAL, SALA, TROCAR };

// Estrutura do botão
struct Botao {
  uint8_t pino;
  Tipo tipo;
  const char* sensor;
  bool soltoAntes;
  unsigned long ultimoAcionamento;
};

// Lista de botões
Botao botoes[] = {
  {12, PRINCIPAL, "A", true, 0},
  {13, PRINCIPAL, "B", true, 0},
  {14, SALA,      "A", true, 0},
  {27, SALA,      "B", true, 0},
  {26, TROCAR,    "",  true, 0},
};
const int TOTAL_BOTOES = sizeof(botoes) / sizeof(botoes[0]);

// Conectar Wi-Fi
void conectarWifi() {
  if (WiFi.status() == WL_CONNECTED) return;
  Serial.print("Conectando ao Wi-Fi");
  WiFi.begin(WIFI_SSID, WIFI_PASS, 6);
  int tentativas = 0;
  while (WiFi.status() != WL_CONNECTED && tentativas++ < 40) {
    delay(250);
    Serial.print(".");
  }
  Serial.println(WiFi.status() == WL_CONNECTED ? " ok" : " falhou");
}

// Enviar evento
void enviarEvento(const char* local, const char* sensor) {
  conectarWifi();
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Sem Wi-Fi: evento perdido");
    return;
  }

  String corpo = String("{\"dispositivoId\":\"") + DEVICE_ID +
                 "\",\"local\":\"" + local +
                 "\",\"sensor\":\"" + sensor + "\"}";

  HTTPClient http;
  WiFiClientSecure seguro;
  WiFiClient simples;

  if (strncmp(API_URL, "https", 5) == 0) {
    seguro.setInsecure();
    http.begin(seguro, API_URL);
  } else {
    http.begin(simples, API_URL);
  }
  http.addHeader("Content-Type", "application/json");
  http.addHeader("ngrok-skip-browser-warning", "true");

  int codigo = http.POST(corpo);
  Serial.printf("[%s %s] HTTP %d -> %s\n", local, sensor, codigo,
                codigo > 0 ? http.getString().c_str() : http.errorToString(codigo).c_str());
  http.end();
}

// Acionar botão
void acionar(const Botao& b) {
  switch (b.tipo) {
    case PRINCIPAL: enviarEvento("PRINCIPAL", b.sensor); break;
    case SALA:      enviarEvento(SALAS[salaAtual], b.sensor); break;
    case TROCAR:
      salaAtual = (salaAtual + 1) % TOTAL_SALAS;
      Serial.printf("Sala selecionada: %s\n", SALAS[salaAtual]);
      break;
  }
}

// Configuração inicial
void setup() {
  Serial.begin(115200);
  for (int i = 0; i < TOTAL_BOTOES; i++) pinMode(botoes[i].pino, INPUT_PULLUP);
  conectarWifi();
  Serial.printf("Pronto. Sala selecionada: %s\n", SALAS[salaAtual]);
  Serial.println("Aperte A e depois B (entrada) ou B e depois A (saida).");
}

// Repetição principal
void loop() {
  unsigned long agora = millis();
  for (int i = 0; i < TOTAL_BOTOES; i++) {
    Botao& b = botoes[i];
    bool solto = digitalRead(b.pino) == HIGH;
    if (b.soltoAntes && !solto && agora - b.ultimoAcionamento > DEBOUNCE_MS) {
      b.ultimoAcionamento = agora;
      acionar(b);
    }
    b.soltoAntes = solto;
  }
  delay(5);
}
