package com.elevator.backend.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MqttConsumer {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    public static final ConcurrentHashMap<String, JSONObject> LATEST_DATA = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        connect();
    }

    public void connect() {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("❌ MQTT 连接断开: " + cause.getMessage());
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    // System.out.println("✅ MQTT 收到消息 | 主题: " + topic + " | 内容: " + payload);
                    handleMessage(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            System.out.println("🔄 正在连接 EMQX... Broker: " + brokerUrl + ", ClientID: " + clientId);
            client.connect(options);
            client.subscribe(topic);
            System.out.println("✅ Java后端 MQTT 连接成功，监听主题: " + topic);

        } catch (MqttException e) {
            System.err.println("❌ MQTT 连接失败! Broker: " + brokerUrl);
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, String payload) {
        try {
            String[] parts = topic.split("/");
            if (parts.length < 2) { // 至少要有两部分才合理
                System.err.println("⚠️ 消息主题格式不正确，忽略: " + topic);
                return;
            }
            // 🟢 修复：总是取主题的最后一部分作为设备ID
            // 兼容 "elevator/uplink/EL-001" 和 "/elevator/uplink/EL-001"
            String deviceCode = parts[parts.length - 1];

            JSONObject json = JSON.parseObject(payload);
            JSONObject values = json.getJSONObject("values");
            Long timestamp = json.getLong("timestamp");

            if (values == null) {
                // System.err.println("⚠️ 消息 Payload 中缺少 'values' 字段，忽略。");
                return;
            }

            values.put("ts", timestamp);
            LATEST_DATA.put(deviceCode, values);
            
            // 打印一条成功日志，确认解析正确 (调试完成后可注释掉)
            // System.out.println("✔️ 更新缓存: Device=" + deviceCode + ", Data=" + values);

        } catch (Exception e) {
            System.err.println("❌ 解析消息失败: " + e.getMessage() + " | Payload: " + payload);
            e.printStackTrace();
        }
    }
}