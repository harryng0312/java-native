package org.harryng.demo.natives;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Db {

    static Logger logger = LoggerFactory.getLogger(Db.class);

    private Connection connection = null;

    protected void initConn() throws SQLException, ClassNotFoundException {
        ResourcesUtil.getProperty("db.username");
        ResourcesUtil.getProperty("db.password");
//        Class.forName(ResourcesUtil.getProperty("db.jdbc.driver"));
//        connection = DriverManager.getConnection(ResourcesUtil.getProperty("db.jdbc.url"),
//                ResourcesUtil.getProperty("db.username"), ResourcesUtil.getProperty("db.password"));
    }

    protected void closeConn() throws SQLException {
        connection.close();
    }

    public void insertDb() {
        logger.info("insert into db");
    }

    public void updateDb() {

    }

    public void deleteDb() {

    }

    public void selectOneDb() {
        final var sql = "select id_, created_date, modified_date, status, screenname," +
                "username, password_, dob, passwd_encrypted_method from user_";
        try {
            initConn();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                logger.info("Screename[" + resultSet.getLong("id_") + "]: " + resultSet.getString("username"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            logger.error("", e);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        } finally {
            try {
                closeConn();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }
}
