package org.harryng.demo.natives;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args){
        logger.info("=====");
        Db db = new Db();
        db.selectOneDb();
    }
}
