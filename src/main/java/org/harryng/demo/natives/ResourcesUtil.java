package org.harryng.demo.natives;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ResourceBundle;

public class ResourcesUtil {

    static ResourceBundle resourceBundle = null;
    static DateTimeFormatter dateTimeFormatter = null;
    static DateTimeFormatter dateFormatter = null;
    static DateTimeFormatter timeFormatter = null;

    static {
        resourceBundle = ResourceBundle.getBundle("config");
    }

    public static String getProperty(String key) {
        return resourceBundle.getString(key);
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        if (dateTimeFormatter == null){
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        }
        return dateTimeFormatter;
    }

    public static DateTimeFormatter getDateFormatter() {
        if (dateFormatter == null){
            dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        }
        return dateFormatter;
    }

    public static DateTimeFormatter getTimeFormatter() {
        if (timeFormatter == null){
            timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }
        return timeFormatter;
    }

}
