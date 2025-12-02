package com.elevator.backend.controller;

import com.elevator.backend.mock.MockDB;
import com.elevator.backend.model.Entities.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    // --- 1. 监控驾驶舱接口 ---
    @GetMapping("/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", MockDB.devices.size());
        stats.put("faults", MockDB.devices.values().stream().filter(d -> "故障".equals(d.getStatus())).count());
        stats.put("onlineRate", 95.5);
        // 模拟 ECharts 趋势数据
        stats.put("trend", Arrays.asList(80, 85, 90, 88, 92, 95, 98));
        return stats;
    }

    // --- 2. 设备管理接口 ---
    @GetMapping("/devices")
    public Collection<Device> getDevices() {
        return MockDB.devices.values();
    }

    // --- 3. 智能派单接口 ---
    @GetMapping("/orders")
    public Collection<WorkOrder> getOrders() {
        return MockDB.orders.values();
    }

    // 派单动作
    @PostMapping("/dispatch")
    public Map<String, String> dispatch(@RequestBody Map<String, String> payload) {
        String id = payload.get("orderId");
        String worker = payload.get("worker");

        WorkOrder order = MockDB.orders.get(id);
        if (order != null) {
            order.setStatus("处理中");
            order.setWorker(worker);
            return Map.of("msg", "success");
        }
        return Map.of("msg", "error");
    }

    // --- 4. 备件接口 ---
    @GetMapping("/parts")
    public Collection<SparePart> getParts() {
        return MockDB.parts.values();
    }
}