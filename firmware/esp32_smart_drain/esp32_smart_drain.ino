#include <WiFi.h>
#include <WiFiClient.h>
#include <PubSubClient.h>
#include <esp_camera.h>
#include <time.h>

/*
 * Target board:
 * - Freenove ESP32-S3 CAM (ESP32-S3-WROOM, OV2640)
 *
 * If camera init fails, open Arduino example:
 *   File > Examples > ESP32 > Camera > CameraWebServer
 * and copy the exact camera pin map used by that sample for your board revision.
 */

// =========================
// User config
// =========================
static const char* WIFI_SSID = "YOUR_WIFI_SSID";
static const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

// IMPORTANT: Use a reachable server address for ESP32 (not localhost).
static const char* SERVER_HOST = "192.168.0.10";
static const uint16_t SERVER_PORT = 8080;

// MQTT broker (readings)
static const char* MQTT_BROKER_HOST = "192.168.0.10";
static const uint16_t MQTT_BROKER_PORT = 1883;
static const char* MQTT_CLIENT_ID = "esp32-smart-drain-01";
static const char* MQTT_TOPIC_PREFIX = "smart-drain/drains";

// HTTP API (photos)
static const char* PHOTOS_PATH = "/api/v1/sensors/photos";

static const long DRAIN_ID = 1;

// Send intervals
static const unsigned long READING_INTERVAL_MS = 10000UL; // 10s
static const unsigned long PHOTO_INTERVAL_MS = 60000UL;   // 60s

// NTP / timezone
static const long GMT_OFFSET_SEC = 9 * 3600; // KST
static const int DAYLIGHT_OFFSET_SEC = 0;

// =========================
// Sensor pin config (example)
// =========================
// IMPORTANT:
// Avoid camera-used pins. These are example GPIOs for external ultrasonic wiring.
// Rewire/change as needed for your board setup.
static const int WATER_TRIG_PIN = 1;
static const int WATER_ECHO_PIN = 2;

static const int TRASH_TRIG_PIN = 41;
static const int TRASH_ECHO_PIN = 42;

// Battery ADC pin (example)
static const int BATTERY_ADC_PIN = 3;

// =========================
// Camera pin config (Freenove ESP32-S3 CAM / ESP32S3_EYE-compatible)
// =========================
#define PWDN_GPIO_NUM     -1
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM     15
#define SIOD_GPIO_NUM      4
#define SIOC_GPIO_NUM      5

#define Y9_GPIO_NUM       16
#define Y8_GPIO_NUM       17
#define Y7_GPIO_NUM       18
#define Y6_GPIO_NUM       12
#define Y5_GPIO_NUM       10
#define Y4_GPIO_NUM        8
#define Y3_GPIO_NUM        9
#define Y2_GPIO_NUM       11
#define VSYNC_GPIO_NUM     6
#define HREF_GPIO_NUM      7
#define PCLK_GPIO_NUM     13

unsigned long lastReadingSentAt = 0;
unsigned long lastPhotoSentAt = 0;
WiFiClient mqttNetClient;
PubSubClient mqttClient(mqttNetClient);

static bool connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) {
    return true;
  }

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  Serial.print("[WiFi] Connecting");
  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 20000UL) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[WiFi] Failed to connect");
    return false;
  }

  Serial.print("[WiFi] Connected, IP: ");
  Serial.println(WiFi.localIP());
  return true;
}

static bool syncTime() {
  configTime(GMT_OFFSET_SEC, DAYLIGHT_OFFSET_SEC, "pool.ntp.org", "time.google.com");

  struct tm timeinfo;
  for (int i = 0; i < 20; ++i) {
    if (getLocalTime(&timeinfo)) {
      Serial.println("[NTP] Time synced");
      return true;
    }
    delay(500);
  }

  Serial.println("[NTP] Time sync failed");
  return false;
}

static String nowIso8601() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    // Fallback if NTP is unavailable
    return "1970-01-01T00:00:00";
  }

  char buf[25];
  strftime(buf, sizeof(buf), "%Y-%m-%dT%H:%M:%S", &timeinfo);
  return String(buf);
}

static float readDistanceCm(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH, 30000UL); // timeout 30ms
  if (duration <= 0) {
    return -1.0f;
  }
  return (duration * 0.0343f) / 2.0f;
}

static float readBatteryPercent() {
  int raw = analogRead(BATTERY_ADC_PIN);
  // Placeholder conversion: tune for your hardware divider/reference.
  float percent = (raw / 4095.0f) * 100.0f;
  if (percent < 0.0f) percent = 0.0f;
  if (percent > 100.0f) percent = 100.0f;
  return percent;
}

static bool ensureMqttConnected() {
  if (mqttClient.connected()) {
    return true;
  }

  Serial.print("[MQTT] Connecting...");
  bool ok = mqttClient.connect(MQTT_CLIENT_ID);
  if (ok) {
    Serial.println("connected");
    return true;
  }

  Serial.print("failed, rc=");
  Serial.println(mqttClient.state());
  return false;
}

static bool publishReadings(float waterLevel, float trashLevel, float batteryLevel, int signalStrength, const String& measuredAt) {
  if (!ensureMqttConnected()) {
    return false;
  }

  String topic = String(MQTT_TOPIC_PREFIX) + "/" + String(DRAIN_ID) + "/readings";
  String payload = "{";
  payload += "\"drainId\":" + String(DRAIN_ID) + ",";
  payload += "\"waterLevel\":" + String(waterLevel, 2) + ",";
  payload += "\"trashLevel\":" + String(trashLevel, 2) + ",";
  payload += "\"batteryLevel\":" + String(batteryLevel, 2) + ",";
  payload += "\"signalStrength\":" + String(signalStrength) + ",";
  payload += "\"measuredAt\":\"" + measuredAt + "\"";
  payload += "}";

  bool ok = mqttClient.publish(topic.c_str(), payload.c_str());
  Serial.printf("[Readings][MQTT] publish %s -> %s\n", ok ? "ok" : "failed", topic.c_str());
  Serial.println(payload);

  return ok;
}

static bool initCamera() {
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;

  if (psramFound()) {
    config.frame_size = FRAMESIZE_SVGA;
    config.jpeg_quality = 10;
    config.fb_count = 2;
  } else {
    config.frame_size = FRAMESIZE_VGA;
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("[Camera] Init failed: 0x%x\n", err);
    return false;
  }

  Serial.println("[Camera] Init ok");
  return true;
}

static bool postPhotoMultipart(long drainId, camera_fb_t* fb) {
  WiFiClient client;

  if (!client.connect(SERVER_HOST, SERVER_PORT)) {
    Serial.println("[Photo] TCP connect failed");
    return false;
  }

  const String boundary = "----ESP32Boundary7MA4YWxkTrZu0gW";
  String partDrain = "--" + boundary + "\r\n"
                     "Content-Disposition: form-data; name=\"drainId\"\r\n\r\n" + String(drainId) + "\r\n";

  String partFileHeader = "--" + boundary + "\r\n"
                          "Content-Disposition: form-data; name=\"imageFile\"; filename=\"capture.jpg\"\r\n"
                          "Content-Type: image/jpeg\r\n\r\n";

  String partTail = "\r\n--" + boundary + "--\r\n";

  size_t contentLength = partDrain.length() + partFileHeader.length() + fb->len + partTail.length();

  client.print(String("POST ") + PHOTOS_PATH + " HTTP/1.1\r\n");
  client.print(String("Host: ") + SERVER_HOST + ":" + SERVER_PORT + "\r\n");
  client.print("Connection: close\r\n");
  client.print(String("Content-Type: multipart/form-data; boundary=") + boundary + "\r\n");
  client.print(String("Content-Length: ") + contentLength + "\r\n\r\n");

  client.print(partDrain);
  client.print(partFileHeader);

  const uint8_t* data = fb->buf;
  size_t remaining = fb->len;
  while (remaining > 0) {
    size_t chunk = remaining > 1024 ? 1024 : remaining;
    client.write(data, chunk);
    data += chunk;
    remaining -= chunk;
  }

  client.print(partTail);

  unsigned long timeoutAt = millis() + 8000UL;
  String response;
  while (client.connected() && millis() < timeoutAt) {
    while (client.available()) {
      response += static_cast<char>(client.read());
    }
  }
  client.stop();

  Serial.println("[Photo] Response:");
  Serial.println(response);

  return response.indexOf(" 200 ") > 0 || response.indexOf(" 201 ") > 0;
}

static bool uploadPhoto() {
  camera_fb_t* fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("[Photo] Capture failed");
    return false;
  }

  bool ok = postPhotoMultipart(DRAIN_ID, fb);
  esp_camera_fb_return(fb);
  return ok;
}

void setup() {
  Serial.begin(115200);
  delay(500);

  pinMode(WATER_TRIG_PIN, OUTPUT);
  pinMode(WATER_ECHO_PIN, INPUT);
  pinMode(TRASH_TRIG_PIN, OUTPUT);
  pinMode(TRASH_ECHO_PIN, INPUT);

  if (!connectWiFi()) {
    Serial.println("[Setup] WiFi not connected. Will retry in loop.");
  }

  mqttClient.setServer(MQTT_BROKER_HOST, MQTT_BROKER_PORT);
  syncTime();
  initCamera();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
    delay(1000);
    return;
  }

  if (!mqttClient.connected()) {
    ensureMqttConnected();
  }
  mqttClient.loop();

  unsigned long nowMs = millis();

  if (nowMs - lastReadingSentAt >= READING_INTERVAL_MS) {
    float waterLevel = readDistanceCm(WATER_TRIG_PIN, WATER_ECHO_PIN);
    float trashLevel = readDistanceCm(TRASH_TRIG_PIN, TRASH_ECHO_PIN);
    float batteryLevel = readBatteryPercent();
    int signalStrength = WiFi.RSSI();
    String measuredAt = nowIso8601();

    Serial.printf("[Readings] water=%.2f trash=%.2f battery=%.2f rssi=%d time=%s\n",
      waterLevel, trashLevel, batteryLevel, signalStrength, measuredAt.c_str());

    publishReadings(waterLevel, trashLevel, batteryLevel, signalStrength, measuredAt);
    lastReadingSentAt = nowMs;
  }

  if (nowMs - lastPhotoSentAt >= PHOTO_INTERVAL_MS) {
    uploadPhoto();
    lastPhotoSentAt = nowMs;
  }

  delay(50);
}
