package components;

import handlers.ClientHandler;
import servises.AuthenticationService;
import servises.impl.DataBaseAuthServiceImp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChatServer {

//    private final AuthenticationService authenticationService = SimpleAuthenticationServiseImpl.getInstance();
    private final AuthenticationService authenticationService;

    {
        try {
            authenticationService = DataBaseAuthServiceImp.getInstance();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<ClientHandler> handlers = new ArrayList<>();

    private int port;


    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("------------------------");
        System.out.println("-----Server started-----");
        System.out.println("------------------------");

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
//        subscribe(handler);
        return handler;
    }

    private Socket conectNewClient(ServerSocket serverSocket) throws IOException {
        System.out.println("Ожидаем нового подключения");
        Socket socket = serverSocket.accept();
        System.out.println("Клиент подключился!");
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
        System.out.println("------------------------");
        System.out.println("-----Server stopped-----");
        System.out.println("------------------------");
        System.exit(0);

    }

    public void broadcastMessage(String message, ClientHandler handler) throws IOException {
        handlers.stream().filter(client -> client != handler).forEach(client -> {
            try {
                client.sendMessageToClient(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void broadcastMessage(String message) throws IOException {
        handlers.stream().forEach(client -> {
            try {
                client.sendMessageToClient(message);
            } catch (IOException e) {
                e.printStackTrace();
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
