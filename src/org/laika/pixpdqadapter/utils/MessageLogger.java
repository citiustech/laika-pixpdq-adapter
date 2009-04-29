package org.laika.pixpdqadapter.utils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhealthexchange.openpixpdq.ihe.impl_v2.hl7.HL7Util;

/**
 *
 * @author kananm
 */
public class MessageLogger {

    private static final Logger log = Logger.getLogger(MessageLogger.class.getName());

    /**
     * insert the message into the db
     *
     * @param msg received message
     */
    public static void log(Message msg) {
        String message = null;
        try {
            message = new HL7Util().encodeMessage(msg);
        } catch (HL7Exception ex) {
            log.log(Level.SEVERE, "SQLException: " + ex.getMessage());
        }

        String sql = "INSERT INTO message_logs (created_at, message) VALUES(" +
                "CURRENT_TIMESTAMP, '" + message + "')";

        DbUtils.getInstance().executeUpdate(sql);
    }

    /**
     * Receives a value for the number of records to be returned,
     * retrieves the records from the database and returns the records as a string.
     *
     * @param rows
     * @return
     */
    public static String getLog(int rows) {

        String retStr = "";

        String sql = "SELECT message, created_at FROM message_logs order by created_at desc limit " + rows;

        try {
            ResultSet rsLog = DbUtils.getInstance().executeQuery(sql);

            while (rsLog.next()) {
                retStr += "\n Log Time: " + rsLog.getString("created_at");
                retStr += "\n Message : " + rsLog.getString("message");
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQLException: " + ex.getMessage());
        }

        return retStr;
    }
}