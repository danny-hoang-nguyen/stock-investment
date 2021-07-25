package danny.stock.calculate.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static final String convertTimeToString(LocalDateTime localDateTime) {
        return Util.DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static final LocalDateTime convertStringToTime(String s) {
        return LocalDateTime.parse(s, DATE_TIME_FORMATTER);
    }

    public static final Long convertDateToSecond(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().toEpochDay() * 24 * 60 * 60;
    }
}
