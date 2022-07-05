import javax.swing.plaf.TableHeaderUI;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final int PORT = 8186;
    private static Socket socket;
    private static DataOutputStream outputStream;
    private static DataInputStream inputStream;

    private static boolean isConnected = false;

    public static void main(String[] args) {

        Thread receivingMessageFromClientThread = new Thread(() -> {
            while (true) {

                try {
                    String message = inputStream.readUTF();
                    System.out.println(message);
                    outputStream.writeUTF("Эхо-ответ сервера: \n" + message);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }
            }

        });
        Thread gettingMessageFromConsoleThread = new Thread(() -> {
            while (true) {
                try {
                    String consoleInput = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    outputStream.writeUTF("Напечатано с консоли сервера: \n" + consoleInput);
                    outputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });



        Thread connectionThread = new Thread(() -> {
            while (true) {

                while (socket == null) {
                    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                        System.out.println("Сервер запущен! Ожидаем соединения");
                        socket = serverSocket.accept();
                        System.out.println("Подключение установлено");
                        outputStream = new DataOutputStream(socket.getOutputStream());
                        inputStream = new DataInputStream(socket.getInputStream());
                        gettingMessageFromConsoleThread.start();
                        receivingMessageFromClientThread.start();
                    } catch (EOFException e) {
                        e.printStackTrace();
                        socket = null;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        });

        connectionThread.start();






    }

}
