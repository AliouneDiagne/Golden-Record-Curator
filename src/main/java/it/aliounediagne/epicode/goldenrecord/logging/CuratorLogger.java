package it.aliounediagne.epicode.goldenrecord.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public final class CuratorLogger {

    
    private static final String LOG_FILE = "curator.log";

    static {
        try {
            
            
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.OFF);
            consoleHandler.setFormatter(new SafeConsoleFormatter());

            
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());

            rootLogger.addHandler(consoleHandler);
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.ALL);

        } catch (IOException ex) {
            
            
            System.err.println("Logging could not be configured: " + ex.getMessage());
        }
    }

    
    private CuratorLogger() {
    }

    public static Logger get(Class<?> owner) {
        return Logger.getLogger(owner.getName());
    }
}
