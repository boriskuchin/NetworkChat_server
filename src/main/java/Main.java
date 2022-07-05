import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final int PORT = 8186;
    private static Socket socket;
    private static DataOutputStream outputStream;
    private static DataInputStream inputStream;

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            while (true) {
                try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                    System.out.println("Сервер запущен! Ожидаем соединения");
                    socket = serverSocket.accept();
                    System.out.println("Подключение установлено");
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    inputStream = new DataInputStream(socket.getInputStream());

                    while (true) {
                        String message = inputStream.readUTF();
                        System.out.println(message);
                        outputStream.writeUTF("Ответ сервера: \n" + message);
                        outputStream.flush();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


    }

}
