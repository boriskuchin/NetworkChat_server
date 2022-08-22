package handlers;

import components.ChatServer;
import components.Prefix;
import components.ProjectLogger;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ClientHandler {

    private final Logger systemLogger;
    private final Logger messageLogger;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy HH:mm:ss", Locale.ROOT);
    Socket socket;
    ChatServer server;
    DataInputStream inputStream;
    DataOutputStream outputStream;
//    private boolean isAuthenticated = false;
    private String userLogin;
    private String userName;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.systemLogger = ProjectLogger.getInstance().getSystemLogger();
        this.messageLogger = ProjectLogger.getInstance().getMessageLogger();
    }

    public void handle()  {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            systemLogger.warn("ошибка чтения потоков");
            systemLogger.warn(ProjectLogger.stackTraceToString(e));
        }

        new Thread(() -> {

            try {
                while (true) {
                    String message = inputStream.readUTF();


                    if (message.length() == 0) {
                        continue;
                    } else if (Prefix.getPrefixFromText(message.trim().split("\\s+")[0]) == null) {
                        continue;
                    } else {
//                        systemLogger.debug(Prefix.getPrefixFromText(message.trim().split("\\s+")[0]));

                        switch (Prefix.getPrefixFromText(message.trim().split("\\s+")[0])) {
                            case AUTH_CMD_PREFIX:
                                if (!authenticateClient(message)) {
                                    continue;
                                }
                                break;
                            case STOP_SERVER_CMD_PREFIX:
                                server.broadcastMessage("ВНИМАНИЕ! ОСТАНОВКА СЕРВЕРА", this);
                                server.stop();
                                break;
                            case CLIENT_MSG_CMD_PREFIX:

                                server.broadcastMessage(String.format("%s%n%s пишет: %s", dateFormat.format(new Date()).toString(), userLogin, message.trim().split("\\s+", 2)[1]), this);
                                messageLogger.info(String.format("%s%n%s пишет: %s", dateFormat.format(new Date()).toString(), userLogin, message.trim().split("\\s+", 2)[1]));
                                break;
                            case END_CLIENT_CMD_PREFIX:
                                server.broadcastMessage("Участник " + userLogin + " вышел из чата", this);
                                server.unsubscribe(this);
                                socket.close();
                                break;
                            case PRIVATE_MSG_CMD_PREFIX:
                                String recepient = message.trim().split("\\s+")[1];
                                ClientHandler receivingHandler = server.getHandlerByName(recepient);
                                if (receivingHandler != this) {
                                    receivingHandler.sendMessageToClient(String.format("%s%n[PM]%s пишет: %s", dateFormat.format(new Date()).toString(), userLogin, message.trim().split("\\s+", 3)[2]));
                                }
                                messageLogger.info(String.format("%s%n[PM]%s пишет: %s", dateFormat.format(new Date()).toString(), userLogin, message.trim().split("\\s+", 3)[2]));
                                break;
                            case NEW_USR_CMD_PREFIX:
                                String login = message.trim().split("\\s+")[1];
                                String name = message.trim().split("\\s+")[2];
                                String pass = message.trim().split("\\s+")[3];
                                systemLogger.info("Добавление нового пользователя");

                                if (!server.isLoginAlreadyExist(login)) {
                                    server.addNewUser(name, login, pass);
                                    outputStream.writeUTF(String.format("%s Пользователь добавлен", Prefix.NEW_USR_OK_CMD_PREFIX.getPrefix()));
                                    systemLogger.info(String.format("%s Пользователь %s добавлен", Prefix.NEW_USR_OK_CMD_PREFIX.getPrefix(), login));
                                } else {
                                    outputStream.writeUTF(String.format("%s пользователь с таким логином уже существует", Prefix.NEW_USR_ERR_CMD_PREFIX.getPrefix()));
                                    systemLogger.info(String.format("%s пользователь с логином %s уже существует", Prefix.NEW_USR_ERR_CMD_PREFIX.getPrefix(), login));

                                }
                                break;
                            case CNG_NAME_CMD_PREFIX:
                                String newName = message.trim().split("\\s+",2)[1];
                                server.changeNameByLogin(userLogin, newName);
                                server.broadcastMessage(String.format("Пользователь %s сменил имя с %s на %s", userLogin,userName,newName), this);
                                systemLogger.info(String.format("Пользователь %s сменил имя с %s на %s", userLogin,userName,newName));
                                break;
                            default:
                                outputStream.writeUTF("Сообщение самому себе" + message);
                        }
                    }
                }


            } catch (IOException e) {
                systemLogger.warn(ProjectLogger.stackTraceToString(e));
                systemLogger.warn("Ошибка чтения потока на хендлере " + this.socket);
                try {
                    server.unsubscribe(this);
                } catch (IOException ex) {
                    systemLogger.warn(ProjectLogger.stackTraceToString(ex));

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

        String authenticatedLogin = server.checkCredentials(messageSplit[1], messageSplit[2]);
        if (authenticatedLogin == null) {
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Некорректное имя пользователя и пароль");
            return false;
        }

        if (server.isUserAlreadyLoggedIn(authenticatedLogin)) {
            outputStream.writeUTF(Prefix.AUTHERR_CMD_PREFIX.getPrefix() + " Пользователь уже залогинен");
            return false;
        }

        userLogin = authenticatedLogin;
        userName = server.getNameByLogin(userLogin);
        outputStream.writeUTF(Prefix.AUTHOK_CMD_PREFIX.getPrefix() + " " + userName);
        server.subscribe(this);

        return true;

    }



    public String getUserLogin() {
        return userLogin;
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
                ", userName='" + userLogin + '\'' +
                '}';
    }
}
