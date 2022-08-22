package components;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ProjectLogger {

    private static ProjectLogger INSTANCE;

    private final Logger systemLogger;
    private final Logger messageLogger;

    private ProjectLogger() {
        PropertyConfigurator.configure("src/main/resources/logs/config/log4j.properties");
        systemLogger = Logger.getLogger("systemLogger");
        messageLogger = Logger.getLogger("messageLogger");
    }

    public Logger getSystemLogger() {
        return systemLogger;
    }

    public Logger getMessageLogger() {
        return messageLogger;
    }

    public static synchronized ProjectLogger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectLogger();
        }
        return INSTANCE;
    }

    public static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
