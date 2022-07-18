package servises.impl;

import models.User;
import servises.AuthenticationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleAuthenticationServiseImpl implements AuthenticationService {
    private static SimpleAuthenticationServiseImpl INSTANCE;
    private List<User> users = new ArrayList<>();

//            List.of(
//            new User("Борис", "Boris", "1"),
//            new User("Анна", "Anna", "1"),
//            new User("Дима", "Dima", "1"),
//            new User("Коля", "Kolya", "1")
//    );
    private SimpleAuthenticationServiseImpl() {
    }

    public static synchronized SimpleAuthenticationServiseImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimpleAuthenticationServiseImpl();
        }
        return INSTANCE;
    }

    @Override
    public String geNameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (login.equals(user.getLogin()) && password.equals(user.getPassword())) {
                return user.getName();
            }
        }
        return null;
    }

    @Override
    public List<String> getLogins() {
        return users.stream().map(user -> user.getLogin()).collect(Collectors.toList());
    }

    @Override
    public void addUser(String name, String login, String pass) {
        users.add(new User(name, login, pass));
    }
}

