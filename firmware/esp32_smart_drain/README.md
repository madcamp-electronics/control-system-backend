# esp32_smart_drain

## File
- `esp32_smart_drain.ino`

## What it does
- Publishes sensor readings to MQTT topic: `smart-drain/drains/{drainId}/readings`
- Uploads camera image to `POST /api/v1/sensors/photos`

## Before upload
1. Open `.ino` in Arduino IDE.
2. Set values:
   - `WIFI_SSID`
   - `WIFI_PASSWORD`
   - `MQTT_BROKER_HOST` (must be reachable from ESP32, not localhost)
   - `MQTT_BROKER_PORT`
   - `MQTT_CLIENT_ID`
   - `SERVER_HOST` (photo upload API host)
   - `SERVER_PORT`
   - `DRAIN_ID`
3. Select board/port and upload.

## Notes
- Camera pin mapping is set for Freenove ESP32-S3 CAM (ESP32-S3 계열 예시값).
- `measuredAt` is generated from NTP time.
- Ultrasonic and battery conversion values are example defaults; tune for your hardware.
- Arduino library required: `PubSubClient` (Nick O'Leary).
