package com.elevator.backend.service;

import org.apache.iotdb.isession.pool.SessionDataSetWrapper;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.tsfile.read.common.RowRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TelemetryService {

    private final SessionPool sessionPool;
    private final String STORAGE_GROUP = "root.elevator_cloud";

    public TelemetryService(SessionPool sessionPool) {
        this.sessionPool = sessionPool;
    }

    /**
     * 查询趋势
     */
    public Map<String, Object> queryTrend(String deviceCode, String measure) {
        // 🟢 临时映射：将前端的 EL-001 映射到 IoTDB 中的 Elevator_A
        // 在正式项目中，这个映射关系应该从 MySQL 数据库中查询
        String targetDeviceName = deviceCode;
        if ("EL-001".equals(deviceCode)) targetDeviceName = "Elevator_A";
        else if ("EL-002".equals(deviceCode)) targetDeviceName = "Elevator_B";
        else if ("EL-003".equals(deviceCode)) targetDeviceName = "Elevator_C";

        // 🟢 修复：实际表名是 Elevator_A，不包含特殊字符，不需要反引号
        String devicePath = STORAGE_GROUP + "." + targetDeviceName;

        // 查询最近 20 个点，倒序查，然后反转给前端
        String sql = String.format("SELECT %s FROM %s ORDER BY time DESC LIMIT 20", measure, devicePath);
        System.out.println("✔️ 执行 IoTDB 查询: " + sql);

        List<String> times = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

        try (SessionDataSetWrapper wrapper = sessionPool.executeQueryStatement(sql)) {
            while (wrapper.hasNext()) {
                RowRecord row = wrapper.next();
                long timestamp = row.getTimestamp();
                // 列表头插，把最新的放在最后，符合前端习惯
                times.add(0, formatter.format(Instant.ofEpochMilli(timestamp)));

                // 获取值
                Object val = row.getFields().get(0).getObjectValue(row.getFields().get(0).getDataType());
                values.add(0, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("times", times);
        result.put("values", values);
        return result;
    }
}