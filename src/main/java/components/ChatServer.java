package components;

import handlers.ClientHandler;
import org.apache.log4j.Logger;
import servises.AuthenticationService;
import servises.impl.DataBaseAuthServiceImp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    private final AuthenticationService authenticationService = DataBaseAuthServiceImp.getInstance();

    private ArrayList<ClientHandler> handlers = new ArrayList<>();

    private int port;

    Logger systemLogger;

    public ChatServer(int port) {
        this.port = port;
        this.systemLogger = ProjectLogger.getInstance().getSystemLogger();

    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        systemLogger.info("------------------------");
        systemLogger.info("-----Server started-----");
        systemLogger.info("------------------------");

        while (true) {
            waitForNewClient(serverSocket);
        }
    }

    private void waitForNewClient(ServerSocket serverSocket) throws IOException {
        Socket socket = conectNewClient(serverSocket);
        ClientHandler handler = createNewHandler(socket);
        handler.handle();
    }

    private ClientHandler createNewHandler(Socket socket) {
        ClientHandler handler = new ClientHandler(socket, this);
        return handler;
    }

    private Socket conectNewClient(ServerSocket serverSocket) throws IOException {
        systemLogger.info("Ожидаем нового подключения");
        Socket socket = serverSocket.accept();
        systemLogger.info("Клиент подключился!");
        return socket;
    }

    public String checkCredentials(String login, String password) {
        return authenticationService.geNameByLoginAndPassword(login, password);
    }

    public boolean isUserAlreadyLoggedIn(String userName) {

        for (ClientHandler handler : handlers) {
            if (handler.getUserLogin() != null && handler.getUserLogin().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLoginAlreadyExist(String login) {

        for (String log : authenticationService.getLogins()) {
            if (log.equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        systemLogger.info("------------------------");
        systemLogger.info("-----Server stopped-----");
        systemLogger.info("------------------------");
        System.exit(0);

    }

    public void broadcastMessage(String message, ClientHandler handler) throws IOException {
        handlers.stream().filter(client -> client != handler).forEach(client -> {
            try {
                client.sendMessageToClient(message);
            } catch (IOException e) {
                systemLogger.error(ProjectLogger.stackTraceToString(e));
            }
        });
    }

    public void broadcastMessage(String message) throws IOException {
        handlers.stream().forEach(client -> {
            try {
                client.sendMessageToClient(message);
            } catch (IOException e) {
                systemLogger.error(ProjectLogger.stackTraceToString(e));
            }
        });
    }


    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        broadcastMessage(String.format("%s вошел в чат",  clientHandler.getUserName()).toUpperCase());
        handlers.add(clientHandler);
        sendUserList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        if (clientHandler.getUserName() != null) {
            broadcastMessage(String.format("%s покинул чат",  clientHandler.getUserName()).toUpperCase());
        }
        handlers.remove(clientHandler);
        sendUserList();
    }

    public ClientHandler getHandlerByName(String name) {
        return handlers.stream().filter(handler -> handler.getUserLogin().equals(name)).findAny().get();
    }

    private void sendUserList() throws IOException {
        String users = Prefix.LIST_CLIENTS_CMD_PREFIX.getPrefix() + " ";
        for (ClientHandler handler : handlers) {
            users += handler.getUserLogin() + " ";
        }
        broadcastMessage(users);

    }

    public void addNewUser(String name, String login, String pass) {
        authenticationService.addUser(name, login, pass);
    }

    public String getNameByLogin(String login) {
        return authenticationService.getNameByLogin(login);
    }

    public void changeNameByLogin(String userLogin, String newName) {
        authenticationService.changeNameByLogin(userLogin, newName);
    }
}
