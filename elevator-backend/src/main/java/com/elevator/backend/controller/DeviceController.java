package com.elevator.backend.controller;

import com.elevator.backend.entity.SysDevice;
import com.elevator.backend.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // 1. 获取所有设备列表
    @GetMapping
    public List<SysDevice> getAllDevices() {
        return deviceService.list();
    }

    // 2. 获取单个设备详情
    @GetMapping("/{id}")
    public SysDevice getDeviceById(@PathVariable String id) {
        // 这里假设前端传的是 deviceCode (如 EL-001)，我们需要查一下
        // 如果前端传的是数据库ID (数字)，直接用 deviceService.getById(id)
        return deviceService.query()
                .eq("device_code", id)
                .one();
    }
}