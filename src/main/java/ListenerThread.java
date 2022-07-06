import java.io.*;
import java.net.Socket;

public class ListenerThread extends Thread {

    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public ListenerThread(Socket socket) {
        super();
        this.socket = socket;
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Подключение установлено. Socket: " + socket);

    }

    @Override
    public void run() {
        Thread t1 = new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    System.out.println(message);
                    outputStream.writeUTF("Эхо-ответ сервера: \n" + message);
                    outputStream.flush();
                }
            } catch (IOException e) {
                System.out.println("Соединение разорвано клиентом");
                e.printStackTrace();

            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
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
                System.out.println("Соединение потеряно");
                e.printStackTrace();
            }
            }
        });


        t2.setDaemon(true);

        t1.start();
        t2.start();
    }


}
