package org.harryng.demo.natives;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Application {

    static System.Logger logger = System.getLogger(Application.class.getCanonicalName());
//    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void reflect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Db db = new Db();
        Method insertDbMethod = db.getClass().getMethod("insertDb");
        logger.log(System.Logger.Level.INFO, "reflection invoked");
        insertDbMethod.invoke(db);
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logger.log(System.Logger.Level.INFO, "=====" + ResourcesUtil.getProperty("db.jdbc.driver"));
//        logger.info("=====");
        Db db = new Db();
        db.insertDb();
    }
}
