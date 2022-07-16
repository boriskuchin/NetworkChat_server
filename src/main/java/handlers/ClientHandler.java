package handlers;

import components.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg";
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg";
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm";
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String LIST_CLIENTS_CMD_PREFIX = "/usrs";

    Socket socket;
    ChatServer server;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    private boolean isAuthenticated = false;
    private String userName;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void handle()  {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ошибка чтения потоков");
            e.printStackTrace();
        }

        new Thread(() -> {

            try {
                while (true) {
                    String message = inputStream.readUTF();
                    if (message.length() == 0) {
                        continue;
                    }
                    if (!isAuthenticated) {
                        if (!authenticateClient(message)) {
                            continue;
                        }

                    } else {
                        switch (message.trim().split("\\s+")[0]) {
                            case STOP_SERVER_CMD_PREFIX:
                                server.broadcastMessage("Остановка сервера", this);
                                server.stop();
                                break;
                            case CLIENT_MSG_CMD_PREFIX:
                                server.broadcastMessage(userName + " пишет: " + message,this);
                                break;
                            case SERVER_MSG_CMD_PREFIX:
                                System.out.println("Сообщение для сервера " + message);
                                break;
                            case END_CLIENT_CMD_PREFIX:
                                server.broadcastMessage("Участник " + userName + " вышел из чата",this);
                                server.unsubscribe(this);
                                socket.close();
                                break;
                            case PRIVATE_MSG_CMD_PREFIX:
                                String recepient = message.trim().split("\\s+")[1];
                                server.getHandlerByName(recepient).sendMessageToClient(userName + " пишет: " + message);
                                break;
                            default: outputStream.writeUTF("Сообщение самому себе" + message);
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка чтения потока на хендлере " + this.socket);
                try {
                    server.unsubscribe(this);
                } catch (IOException ex) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private boolean authenticateClient(String message) throws IOException {
        String[] messageSplit = message.trim().split("\\s+");
        if (messageSplit.length != 3) {
            outputStream.writeUTF(AUTHERR_CMD_PREFIX + " Неверный формат строки аутентификации");
            return false;
        }

        if (!messageSplit[0].equals(AUTH_CMD_PREFIX)) {
            outputStream.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            return false;
        }

        String authenticatedName = server.checkCredentials(messageSplit[1], messageSplit[2]);
        if (authenticatedName == null) {
            outputStream.writeUTF(AUTHERR_CMD_PREFIX + " Некорректное имя пользователя и пароль");
            return false;
        }

        if (server.isUserAlreadyLoggedIn(authenticatedName)) {
            outputStream.writeUTF(AUTHERR_CMD_PREFIX + " Пользователь уже залогинен");
            return false;
        }

        userName = authenticatedName;
        outputStream.writeUTF(AUTHOK_CMD_PREFIX + " Добро пожаловать " + userName);
        isAuthenticated = true;
        server.subscribe(this);

        return true;

    }



    public String getUserName() {
        return userName;
    }

    public void sendMessageToClient(String message) throws IOException {
        outputStream.writeUTF(message);
    }

    @Override
    public String toString() {
        return "ClientHandler{" +
                "socket=" + socket +
                ", server=" + server +
                ", inputStream=" + inputStream +
                ", outputStream=" + outputStream +
                ", isAuthenticated=" + isAuthenticated +
                ", userName='" + userName + '\'' +
                '}';
    }
}
