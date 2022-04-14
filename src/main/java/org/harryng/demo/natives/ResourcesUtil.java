package org.harryng.demo.natives;

import java.util.ResourceBundle;

public class ResourcesUtil {

    static ResourceBundle resourceBundle = null;

    static{
        resourceBundle = ResourceBundle.getBundle("config");
    }

    public static String getProperty(String key){
        return resourceBundle.getString(key);
    }
}
