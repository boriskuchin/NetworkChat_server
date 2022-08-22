import components.ProjectLogger;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ListenerThread extends Thread {

    private final Logger systemLogger;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public ListenerThread(Socket socket) {
        super();
        this.socket = socket;
        this.systemLogger = ProjectLogger.getInstance().getSystemLogger();
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }
        systemLogger.info("Подключение установлено. Socket: " + socket);

    }

    @Override
    public void run() {
        Thread t1 = new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    systemLogger.debug(message);
                    outputStream.writeUTF("Эхо-ответ сервера: \n" + message);
                    outputStream.flush();
                }
            } catch (IOException e) {
                systemLogger.info("Соединение разорвано клиентом");
                systemLogger.info(ProjectLogger.stackTraceToString(e));

            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    systemLogger.error(ProjectLogger.stackTraceToString(ex));
                }
            }
        });

        Thread t2 = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!socket.isClosed()) {
            try {

                    while (reader.ready()) {
                        String consoleInput = reader.readLine();
                        outputStream.writeUTF("Напечатано с консоли сервера: \n" + consoleInput);
                        outputStream.flush();
                    }


            } catch (IOException e) {
                systemLogger.error("Соединение потеряно");
                systemLogger.error(ProjectLogger.stackTraceToString(e));
            }
            }
        });


        t2.setDaemon(true);

        t1.start();
        t2.start();
    }


}
