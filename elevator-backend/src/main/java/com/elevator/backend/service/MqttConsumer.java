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

    // 🟢 核心新增：内存缓存，用于存储所有设备的最新状态
    // Key: 设备编码 (如 "EL-001")
    // Value: 最新的一条 JSON 数据
    // public static 方便 Controller 直接读取
    public static final ConcurrentHashMap<String, JSONObject> LATEST_DATA = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        connect();
    }

    public void connect() {
        try {
            // 1. 创建客户端
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            // 2. 连接参数
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true); // 自动重连
            // options.setUserName("admin"); // 如果EMQX设了密码，请取消注释
            // options.setPassword("public".toCharArray());

            // 3. 回调处理
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("❌ MQTT 连接断开: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    handleMessage(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            // 4. 执行连接
            System.out.println("🔄 正在连接 EMQX...");
            client.connect(options);
            client.subscribe(topic);
            System.out.println("✅ Java后端 MQTT 连接成功，监听主题: " + topic);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理消息核心逻辑
     */
    private void handleMessage(String topic, String payload) {
        try {
            // 1. 从主题解析设备ID
            // Topic: elevator/uplink/EL-001
            String[] parts = topic.split("/");
            if (parts.length < 3) return;
            String deviceCode = parts[2]; // 获取 "EL-001"

            // 2. 解析 JSON
            JSONObject json = JSON.parseObject(payload);
            JSONObject values = json.getJSONObject("values"); // 获取具体的测点数据
            Long timestamp = json.getLong("timestamp");

            if (values == null) return;

            // 3. 🟢 更新内存缓存 (用于前端实时展示，不走数据库，速度快)
            // 我们把 timestamp 也放进去
            values.put("ts", timestamp);
            LATEST_DATA.put(deviceCode, values);

            // 4. 打印日志 (调试用)
            // System.out.println("⚡ 收到实时数据 [" + deviceCode + "]: " + values);

            // 5. ❌ 不再写入 IoTDB
            // 因为我们已经配置了 EMQX 规则引擎直接入库，Java 这里就不需要再写了，避免重复。

            // 6. TODO: 告警判断逻辑 (下一步我们可以在这里做)
            // checkAlarm(deviceCode, values);

        } catch (Exception e) {
            System.err.println("解析消息失败: " + e.getMessage() + " | Payload: " + payload);
        }
    }
}