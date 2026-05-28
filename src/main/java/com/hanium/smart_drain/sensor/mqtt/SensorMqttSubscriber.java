package com.hanium.smart_drain.sensor.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.smart_drain.sensor.dto.SensorReadingRequest;
import com.hanium.smart_drain.sensor.service.SensorService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorMqttSubscriber {

    private final SensorService sensorService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.enabled}")
    private boolean mqttEnabled;

    @Value("${mqtt.broker-uri}")
    private String brokerUri;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic.readings}")
    private String readingsTopic;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    private MqttAsyncClient client;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!mqttEnabled) {
            log.info("MQTT subscriber disabled (mqtt.enabled=false)");
            return;
        }

        try {
            String effectiveClientId = clientId + "-" + UUID.randomUUID();
            client = new MqttAsyncClient(brokerUri, effectiveClientId, new MemoryPersistence());
            client.setCallback(new SensorMqttCallback());
            client.connect(buildConnectOptions()).waitForCompletion(10_000);
            log.info("MQTT connected. broker={}, topic={}", brokerUri, readingsTopic);
        } catch (Exception e) {
            log.error("Failed to start MQTT subscriber", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(5_000);
            }
            client.close();
        } catch (Exception e) {
            log.warn("Failed to close MQTT client cleanly", e);
        }
    }

    private MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        if (username != null && !username.isBlank()) {
            options.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            options.setPassword(password.toCharArray());
        }
        return options;
    }

    private void subscribeReadingsTopic() throws MqttException {
        if (client != null && client.isConnected()) {
            client.subscribe(readingsTopic, qos).waitForCompletion(10_000);
            log.info("MQTT subscribed: topic={}, qos={}", readingsTopic, qos);
        }
    }

    private void handleReadingMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        try {
            JsonNode json = objectMapper.readTree(payload);

            Long drainId = getLong(json, "drainId");
            if (drainId == null) {
                drainId = parseDrainIdFromTopic(topic);
            }
            if (drainId == null) {
                log.warn("MQTT reading skipped: drainId missing, topic={}, payload={}", topic, payload);
                return;
            }

            Double waterLevel = getDouble(json, "waterLevel");
            Double trashLevel = getDouble(json, "trashLevel");
            Double batteryLevel = getDouble(json, "batteryLevel");
            Integer signalStrength = getInt(json, "signalStrength");
            LocalDateTime measuredAt = parseMeasuredAt(json.get("measuredAt"));

            if (waterLevel == null || trashLevel == null || batteryLevel == null || signalStrength == null || measuredAt == null) {
                log.warn("MQTT reading skipped: required fields missing, topic={}, payload={}", topic, payload);
                return;
            }

            SensorReadingRequest request = SensorReadingRequest.builder()
                .drainId(drainId)
                .waterLevel(waterLevel)
                .trashLevel(trashLevel)
                .batteryLevel(batteryLevel)
                .signalStrength(signalStrength)
                .measuredAt(measuredAt)
                .build();

            sensorService.ingestReading(request);
        } catch (Exception e) {
            log.error("Failed to process MQTT reading. topic={}, payload={}", topic, payload, e);
        }
    }

    private Long parseDrainIdFromTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }
        String[] parts = topic.split("/");
        if (parts.length < 4) {
            return null;
        }
        // Expected: smart-drain/drains/{drainId}/readings
        try {
            if ("drains".equals(parts[1])) {
                return Long.valueOf(parts[2]);
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private Long getLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asLong();
    }

    private Double getDouble(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asDouble();
    }

    private Integer getInt(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private LocalDateTime parseMeasuredAt(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(text).toLocalDateTime();
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private class SensorMqttCallback implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            try {
                subscribeReadingsTopic();
                if (reconnect) {
                    log.info("MQTT reconnected. server={}", serverURI);
                }
            } catch (Exception e) {
                log.error("Failed to subscribe after MQTT connect/reconnect", e);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT connection lost", cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            handleReadingMessage(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Subscriber side: no-op.
        }
    }
}
