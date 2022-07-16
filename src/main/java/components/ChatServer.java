package components;

import handlers.ClientHandler;
import servises.AuthenticationService;
import servises.impl.SimpleAuthenticationServiseImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    private final AuthenticationService authenticationService = SimpleAuthenticationServiseImpl.getInstance();
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
            if (handler.getUserName() != null && handler.getUserName().equals(userName)) {
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
        broadcastMessage(String.format("%s %s вошел в чат",Prefixes.SERVER_MSG_CMD_PREFIX.getPrefix(), clientHandler.getUserName()));
        handlers.add(clientHandler);
        sendUserList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        broadcastMessage(String.format("%s %s покинул чат",Prefixes.SERVER_MSG_CMD_PREFIX.getPrefix(), clientHandler.getUserName()));
        handlers.remove(clientHandler);
        sendUserList();
    }

    public ClientHandler getHandlerByName(String name) {
        return handlers.stream().filter(handler -> handler.getUserName().equals(name)).findAny().get();
    }

    private void sendUserList() throws IOException {
        String users = Prefixes.LIST_CLIENTS_CMD_PREFIX.getPrefix() + " ";
        for (ClientHandler handler : handlers) {
            users += handler.getUserName() + " ";
        }
        broadcastMessage(users);

    }

}
