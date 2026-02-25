package com.ai.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UUIDUtil {
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT);
    public static synchronized String getUUID(String prefix) {
        String timePart = LocalDateTime.now().format(DTF);
        Random random = new Random();
        int rand = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix+timePart+String.format("%d", rand);
    }

}
