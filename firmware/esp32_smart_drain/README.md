# esp32_smart_drain

## File
- `esp32_smart_drain.ino`

## What it does
- Sends sensor readings to `POST /api/v1/sensors/readings`
- Uploads camera image to `POST /api/v1/sensors/photos`

## Before upload
1. Open `.ino` in Arduino IDE.
2. Set values:
   - `WIFI_SSID`
   - `WIFI_PASSWORD`
   - `SERVER_HOST` (must be reachable from ESP32, not localhost)
   - `SERVER_PORT`
   - `DRAIN_ID`
3. Select board/port and upload.

## Notes
- Camera pin mapping is set for ESP32-CAM AI Thinker.
- `measuredAt` is generated from NTP time.
- Ultrasonic and battery conversion values are example defaults; tune for your hardware.
