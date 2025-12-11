package com.elevator.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.elevator.backend.service.MqttConsumer;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class DeviceStatusController {

    // 获取所有设备的最新实时状态快照
    // 前端看板页可能会用到
    @GetMapping("/all")
    public Map<String, JSONObject> getAllStatus() {
        return MqttConsumer.LATEST_DATA;
    }

    // 获取单个设备的最新实时状态
    // 前端详情页用这个
    @GetMapping("/{deviceId}")
    public JSONObject getDeviceStatus(@PathVariable String deviceId) {
        // 如果缓存里没有（比如刚启动），返回一个空对象或默认值，防止前端报错
        return MqttConsumer.LATEST_DATA.getOrDefault(deviceId, new JSONObject());
    }
}