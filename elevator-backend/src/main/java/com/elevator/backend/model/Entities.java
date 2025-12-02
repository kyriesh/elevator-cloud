package com.elevator.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 这是一个包含所有实体类的容器文件
public class Entities {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Device {
        private String id;
        private String name;
        private String status; // 正常, 故障, 离线
        private String location;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkOrder {
        private String id;
        private String deviceName;
        private String issue;
        private String status; // 待派单, 处理中, 已完成
        private String priority; // 紧急, 普通
        private String worker;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SparePart {
        private String id;
        private String name;
        private int stock;
        private int threshold; // 预警阈值
    }
}