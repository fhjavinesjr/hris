package com.timekeeping.utilities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class UseUtils {

    public static LocalDateTime getLocalDateTimeNow() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

}
