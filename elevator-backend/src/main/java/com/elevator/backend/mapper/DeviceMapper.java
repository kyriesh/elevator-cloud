package com.elevator.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elevator.backend.entity.SysDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<SysDevice> {
    // 这里什么都不用写，MyBatis-Plus 已经帮你写好了 CRUD
}