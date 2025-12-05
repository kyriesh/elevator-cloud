package com.elevator.backend.controller;

import com.elevator.backend.service.TelemetryService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    // API: /api/telemetry/EL-001/speed
    @GetMapping("/{deviceId}/{measure}")
    public Map<String, Object> getTrend(@PathVariable String deviceId, @PathVariable String measure) {
        // 前端传过来的是 EL-001，Service 会自动转成 EL_001 查库
        return telemetryService.queryTrend(deviceId, measure);
    }
}