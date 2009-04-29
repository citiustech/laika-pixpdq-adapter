package org.laika.pixpdqadapter.start;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openhealthexchange.openpixpdq.ihe.configuration.ConfigurationLoader;
import org.openhealthexchange.openpixpdq.ihe.configuration.IheConfigurationException;
import org.openhealthexchange.openpixpdq.ihe.impl_v2.PixManager;

/**
 *
 * @author abhijeetl
 */
public class PixPdqServer extends Thread {

    public static final String CONF_FILE = "conf/actors/IheActors.xml";
    public static final String FILENAME = "pixpdq.str";
    public static final int WAITTIME = 3000;
    private static Logger logger = Logger.getLogger(PixPdqServer.class);
    private static PixManager actor;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File confFile = new File(CONF_FILE);
        String mode = "both standard and secure";

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("Secure")) {
                confFile = new File("conf/actors/IheActorsSecure.xml");
                mode = "secure";
            } else if (args[0].equalsIgnoreCase("Standard")) {
                confFile = new File("conf/actors/IheActorsStandard.xml");
                mode = "standard";
            }
        }

        try {
            //ConfigurationLoader.getInstance().loadConfiguration();
            createFile();
            new PixPdqServer().start();
            ConfigurationLoader loader = ConfigurationLoader.getInstance();

            loader.loadConfiguration(confFile.getAbsolutePath(), true);


            System.out.println("OpenPIXPDQ server started successfully in " + mode + " mode.");
        } catch (IheConfigurationException ex) {
            logger.log(Level.FATAL, null, ex);
        }
    }

    private static void createFile() {
        File file = new File(FILENAME);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            logger.log(Level.FATAL, null, ex);
        }
    }

    public void stopServer() {
        File file = new File(FILENAME);
        while (file.exists()) {
            try {
                Thread.sleep(WAITTIME);
            } catch (InterruptedException ex) {
                logger.log(Level.FATAL, null, ex);
            }
        }
        System.out.println("Stopping PIXPDQ server...");
        try {
            ConfigurationLoader.getInstance().resetConfiguration(null, null);
        } catch (IheConfigurationException ex) {
            logger.log(Level.FATAL, null, ex);
        }
    }

    public void run() {
        stopServer();
    }
}
