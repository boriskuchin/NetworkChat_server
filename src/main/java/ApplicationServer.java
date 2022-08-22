import components.ChatServer;
import components.ProjectLogger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;



public class ApplicationServer {


    private static String propPath;
    private static final int DEFAULT_PORT = 9999;
     private static  int port;

    public static void main(String[] args) {

        Logger systemLogger = ProjectLogger.getInstance().getSystemLogger();

        propPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propPath + "application.properties"));
            port = Integer.parseInt(properties.getProperty("server.port"));
        } catch (IOException  | NumberFormatException exception) {
            systemLogger.warn(exception);
            systemLogger.warn("Значение из конфига не подтянулись. Присвоен дефолтны порт " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        try {
            new ChatServer(port).start();
        } catch (IOException e) {
            systemLogger.error("Ошибка запуска сервера");
            systemLogger.error(e.getMessage());
        }
    }

}
