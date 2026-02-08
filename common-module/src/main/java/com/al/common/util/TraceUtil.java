package com.al.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceUtil {
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT);

    public static String createTraceId() {
        // 1. 时间戳
        String timePart = LocalDateTime.now().format(DTF);

        // 2. 原子序列（0 ~ 9999）
        int seq = SEQ.getAndIncrement();
        if (seq >= 10000) {
            SEQ.compareAndSet(seq + 1, 0); // 防止溢出
            seq = seq % 10000;
        }

        // 3. 随机补充（ThreadLocalRandom，非 Random）
        int rand = ThreadLocalRandom.current().nextInt(1000, 10000);

        return timePart
                + rand
                + String.format("%04d", seq);
    }
}
