package org.harryng.demo.natives;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Application {

    static System.Logger logger = System.getLogger(Application.class.getCanonicalName());
//    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.log(System.Logger.Level.INFO, "=====" + ResourcesUtil.getProperty("db.jdbc.driver"));
//        logger.info("=====");
//        Db db = new Db();
//        db.selectOneDb();
    }
}
