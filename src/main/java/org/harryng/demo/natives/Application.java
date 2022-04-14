package org.harryng.demo.natives;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Application {

    //    static System.Logger logger = System.getLogger(Application.class.getCanonicalName());
    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void reflect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Db db = new Db();
        Method insertDbMethod = db.getClass().getMethod("insertDb");
//        logger.log(System.Logger.Level.INFO, "reflection invoked");
        logger.info("reflection invoked");
        insertDbMethod.invoke(db);
    }

    public static void accessResource() {
//        logger.log(System.Logger.Level.INFO, "Resource properties:" + ResourcesUtil.getProperty("db.jdbc.driver"));
        logger.info("Resource properties:" + ResourcesUtil.getProperty("db.jdbc.driver"));
    }

    public static void accessDb() {
        var db = new Db();
        db.selectOneDb();
    }

    public static void accessFile() throws IOException {
        var fileAccess = new FileAccession();
        fileAccess.writeFile();
        fileAccess.readFile();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
//        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
//        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
//        logger.log(System.Logger.Level.INFO, "=====");
//        File logbackFile = new File("config", "logback.xml_");
//        System.setProperty("logback.configurationFile", logbackFile.getAbsolutePath());
//        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
//        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
        logger.info("=====");

    }
}
