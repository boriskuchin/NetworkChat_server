package handlers;

import components.ChatServer;
import components.Prefix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler {


    Socket socket;
    ChatServer server;
    DataInputStream inputStream;
    DataOutputStream outputStream;
//    private boolean isAuthenticated = false;
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
                    System.out.println(message);

                    if (message.length() == 0) {
                        continue;
                    } else {
                        switch (Prefix.getPrefixFromText(message.trim().split("\\s+")[0])) {
                            case AUTH_CMD_PREFIX:
                                if (!authenticateClient(message)) {
                                    continue;
                                }
                                break;
                            case STOP_SERVER_CMD_PREFIX:
                                server.broadcastMessage("Остановка сервера", this);
                                server.stop();
                                break;
                            case CLIENT_MSG_CMD_PREFIX:
                                server.broadcastMessage(userName + " пишет: " + message, this);
                                break;
                            case SERVER_MSG_CMD_PREFIX:
                                System.out.println("Сообщение для сервера " + message);
                                break;
                            case END_CLIENT_CMD_PREFIX:
                                server.broadcastMessage("Участник " + userName + " вышел из чата", this);
                                server.unsubscribe(this);
                                socket.close();
                                break;
                            case PRIVATE_MSG_CMD_PREFIX:
                                String recepient = message.trim().split("\\s+")[1];
                                server.getHandlerByName(recepient).sendMessageToClient(userName + " пишет: " + message);
                                break;
                            case NEW_USR_CMD_PREFIX:
                                String login = message.trim().split("\\s+")[1];
                                String name = message.trim().split("\\s+")[2];
                                String pass = message.trim().split("\\s+")[3];
                                if (!server.isLoginAlreadyExist(login)) {
                                    server.addNewUser(name, login, pass);
                                    outputStream.writeUTF(String.format("%s Пользователь добавлен", Prefix.NEW_USR_OK_CMD_PREFIX.getPrefix()));
                                } else {
                                    outputStream.writeUTF(String.format("%s пользователь с таким логином уже существует", Prefix.NEW_USR_ERR_CMD_PREFIX.getPrefix()));
                                }
                                break;
                            default:
                                outputStream.writeUTF("Сообщение самому себе" + message);
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
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Неверный формат строки аутентификации");
            return false;
        }

        if (!messageSplit[0].equals(Prefix.AUTH_CMD_PREFIX.getPrefix())) {
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Неверная команда аутентификации");
            return false;
        }

        String authenticatedName = server.checkCredentials(messageSplit[1], messageSplit[2]);
        if (authenticatedName == null) {
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Некорректное имя пользователя и пароль");
            return false;
        }

        if (server.isUserAlreadyLoggedIn(authenticatedName)) {
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Пользователь уже залогинен");
            return false;
        }

        userName = authenticatedName;
        outputStream.writeUTF(Prefix.AUTHOK_CMD_PREFIX.getPrefix() + " Добро пожаловать " + userName);
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
                ", userName='" + userName + '\'' +
                '}';
    }
}
