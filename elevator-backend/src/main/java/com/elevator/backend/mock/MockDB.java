package com.elevator.backend.mock;

import com.elevator.backend.model.Entities.*; // 导入刚才的实体
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockDB {
    // 线程安全的 Map 模拟数据库表
    public static final Map<String, Device> devices = new ConcurrentHashMap<>();
    public static final Map<String, WorkOrder> orders = new ConcurrentHashMap<>();
    public static final Map<String, SparePart> parts = new ConcurrentHashMap<>();

    // 构造函数：启动时初始化一些假数据
    public MockDB() {
        // 1. 初始化设备
        devices.put("DEV001", new Device("DEV001", "1号楼-客梯-A", "正常", "A栋-1单元"));
        devices.put("DEV002", new Device("DEV002", "3号楼-货梯-B", "故障", "A栋-3单元"));
        devices.put("DEV003", new Device("DEV003", "5号楼-消防梯", "离线", "B栋-地下"));
        devices.put("DEV004", new Device("DEV004", "2号楼-观光梯", "正常", "C栋-大厅"));

        // 2. 初始化工单
        orders.put("WO001", new WorkOrder("WO001", "3号楼-货梯-B", "门锁回路异常 (E51)", "待派单", "紧急", null));
        orders.put("WO002", new WorkOrder("WO002", "5号楼-消防梯", "心跳丢失", "处理中", "普通", "张伟"));

        // 3. 初始化库存
        parts.put("P001", new SparePart("P001", "门机皮带", 15, 10));
        parts.put("P002", new SparePart("P002", "主控板", 3, 5)); // 低库存！
        parts.put("P003", new SparePart("P003", "光幕传感器", 20, 8));
    }
}