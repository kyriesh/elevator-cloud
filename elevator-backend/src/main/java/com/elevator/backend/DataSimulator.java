//package com.elevator.backend;
//
//import com.elevator.backend.service.TelemetryService;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Random;
//
//@Component
//@EnableScheduling // 开启定时任务
//public class DataSimulator {
//
//    private final TelemetryService telemetryService;
//    private final Random random = new Random();
//
//    public DataSimulator(TelemetryService telemetryService) {
//        this.telemetryService = telemetryService;
//    }
//
//    // 每 5 秒执行一次
//    @Scheduled(fixedRate = 5000)
//    public void generateData() {
//        long now = System.currentTimeMillis();
//
//        // 模拟两台电梯的数据
//        String[] devices = {"EL-001", "EL-002"};
//
//        for (String dev : devices) {
//            // 1. 模拟速度 (0.0 ~ 2.0 m/s 波动)
//            double speed = 1.0 + (random.nextDouble() - 0.5);
//            telemetryService.insertRecord(dev, now, "speed", Math.abs(speed)); // 速度取绝对值
//
//            // 2. 模拟温度 (25 ~ 30 度)
//            double temp = 25.0 + random.nextDouble() * 5;
//            telemetryService.insertRecord(dev, now, "temperature", temp);
//
//            // 3. 模拟震动 (0.01 ~ 0.05 g)
//            double vibration = 0.01 + random.nextDouble() * 0.04;
//            telemetryService.insertRecord(dev, now, "vibration", vibration);
//        }
//
//         System.out.println("已写入模拟数据至 IoTDB: " + now);
//    }
//}