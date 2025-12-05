package com.elevator.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_device") // 对应数据库表名
public class SysDevice {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String deviceCode;    // 设备编号 (EL-001)
    private String name;          // 设备名称
    private String type;          // 类型 (Elevator/Gateway)
    private String brand;         // 品牌
    private String model;         // 型号
    private String address;       // 安装地址
    private String status;        // 状态 (在线/故障)
    private LocalDate installDate;// 投产日期
    private String maintenanceUnit;// 维保单位
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}