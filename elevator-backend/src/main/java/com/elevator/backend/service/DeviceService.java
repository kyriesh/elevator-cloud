package com.elevator.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevator.backend.entity.SysDevice;
import com.elevator.backend.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

@Service
public class DeviceService extends ServiceImpl<DeviceMapper, SysDevice> {
    // 同样，继承 ServiceImpl 后，你直接拥有了 save, list, getById 等方法
}