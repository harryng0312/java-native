package org.harryng.demo.natives;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public static void accessResource(){
        logger.log(System.Logger.Level.INFO, "Resource properties:" + ResourcesUtil.getProperty("db.jdbc.driver"));
    }

    public static void accessDb(){
        var db = new Db();
        db.selectOneDb();
    }

    public static void accessFile() throws IOException {
        var fileAccess = new FileAccession();
        fileAccess.writeFile();
        fileAccess.readFile();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        logger.log(System.Logger.Level.INFO, "=====");
//        logger.info("=====");

    }
}
