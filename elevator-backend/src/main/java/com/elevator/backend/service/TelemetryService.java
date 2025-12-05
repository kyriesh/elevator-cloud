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
    // 注意：这里不用加前缀 root.elevator_cloud，因为我们在 SQL 里拼
    private final String STORAGE_GROUP = "root.elevator_cloud";

    public TelemetryService(SessionPool sessionPool) {
        this.sessionPool = sessionPool;
    }

    // 以前的 insertRecord 方法可以删掉，反正不用了

    /**
     * 查询趋势 (使用 ALIGN BY DEVICE 语法，完美解决 null 问题)
     */
    public Map<String, Object> queryTrend(String deviceCode, String measure) {
        // SQL: select speed from root.elevator_cloud.EL_001 order by time desc limit 100
        // 注意：IoTDB 的路径不支持中划线，如果你的 deviceCode 是 EL-001，要转成 EL_001
        String devicePath = STORAGE_GROUP + "." + deviceCode.replaceAll("-", "_");

        // 查询最近 20 个点，倒序查，然后反转给前端
        String sql = String.format("SELECT %s FROM %s ORDER BY time DESC LIMIT 20", measure, devicePath);

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