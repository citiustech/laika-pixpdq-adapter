package org.laika.pixpdqadapter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author kananm
 */
public class DbUtils {

    public String _dbUrl;
    private String _userId;
    private String _password;
    private String _dbDriver;
    private Connection conn;
    private static DbUtils instance;

    private static final Logger log = Logger.getLogger(DbUtils.class.getName());

    /**
     *
     * @return an instance of the DBUtils class
     */
    public static DbUtils getInstance(){
        if( instance == null)
            instance = new DbUtils();
        return instance;
    }

    /**
     * initialize the database connection
     */
    private DbUtils() {

        Properties tProps = new Properties();

        InputStream is = null;
        try {
            is = new FileInputStream(new File("./conf/db.properties"));
            //getClass().getResourceAsStream("/conf/db.properties");
            tProps.load(is);
            is.close();
        } catch (FileNotFoundException ex) {
            log.error("FileNotFoundException: " + ex.getMessage());
        }
        catch (IOException ex) {
            log.error("IOException: " + ex.getMessage());
        }

        _dbUrl = tProps.getProperty("url");
        if (_dbUrl == null) {
        }
        _userId = tProps.getProperty("userid");
        _password = tProps.getProperty("password");
        _dbDriver = tProps.getProperty("driver");
        try {
            Class.forName(_dbDriver);
        } catch (ClassNotFoundException ex) {
            log.error("ClassNotFoundException: " + ex.getMessage());
        }
        
    }

    /**
     * close the database connection
     */
    public void closeConnection() {
        try {
            if ((conn != null) || (!conn.isClosed())) {
                conn.close();
            }
        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
        }
    }

    /**
     * open the database connection
     */
    public void openConnection() {
        try {
            conn = DriverManager.getConnection(_dbUrl, _userId, _password);
        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
        }
    }

    /**
     *
     * @param str query to be executed
     * @return resultset
     */
    public ResultSet executeQuery(String str) {
        Statement stmt = null;
        ResultSet res = null;

        try {

            if (conn != null && (!conn.isClosed())) {
                stmt = conn.createStatement();
                res = stmt.executeQuery(str);
            } else {
                System.err.println("Invalid Connection");
            }
        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
        }
        return res;
    }

    // 
    /**
     *
     * @param str query to be executed
     * @return
     */
    public int executeUpdate(String str) {
        Statement stmt = null;
        int res = 0;
        try {

            openConnection();

            if (conn != null && (!conn.isClosed())) {
                stmt = conn.createStatement();
                res = stmt.executeUpdate(str);

                closeConnection();
            } else {
                System.err.println("Invalid Connection");
            }
        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
        }
        return res;
    }
}