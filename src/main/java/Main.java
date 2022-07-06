import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final int PORT = 8186;
    private static Socket socket;
    private static DataOutputStream outputStream;
    private static DataInputStream inputStream;

    public static void main(String[] args) {


        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен! Ожидаем соединения");
            while (true) {
                new ListenerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
