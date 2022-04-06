package org.harryng.demo.natives;

import org.h2.jdbc.JdbcConnection;

import java.sql.*;
import java.util.Properties;

public class Db {

    static System.Logger logger = System.getLogger(Db.class.getCanonicalName());

    private Connection connection = null;

    protected void initConn() throws SQLException, ClassNotFoundException {
        var driver = new org.h2.Driver();
        connection = new JdbcConnection(ResourcesUtil.getProperty("db.jdbc.url"), new Properties(),
                ResourcesUtil.getProperty("db.username"), ResourcesUtil.getProperty("db.password"), false);
//        Class.forName(ResourcesUtil.getProperty("db.jdbc.driver"));
//        connection = DriverManager.getConnection(ResourcesUtil.getProperty("db.jdbc.url"),
//                ResourcesUtil.getProperty("db.username"), ResourcesUtil.getProperty("db.password"));
    }

    protected void closeConn() throws SQLException {
        connection.close();
    }

    public void insertDb(){

    }

    public void updateDb(){

    }

    public void deleteDb(){

    }

    public void selectOneDb(){
        final var sql = "select id_, created_date, modified_date, status, screenname," +
                "username, password_, dob, passwd_encrypted_method from user_";
        try {
            initConn();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                logger.log(System.Logger.Level.INFO, "Screename["+ resultSet.getLong("id_") +"]: " + resultSet.getString("username"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            logger.log(System.Logger.Level.ERROR, "", e);
        } catch (ClassNotFoundException e) {
            logger.log(System.Logger.Level.ERROR, "", e);
        } finally {
            try {
                closeConn();
            } catch (SQLException e) {
                logger.log(System.Logger.Level.ERROR, "", e);
            }
        }
    }
}
