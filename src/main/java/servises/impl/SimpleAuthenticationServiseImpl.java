package servises.impl;

import models.User;
import servises.AuthenticationService;

import java.util.List;

public class SimpleAuthenticationServiseImpl implements AuthenticationService {
    private static SimpleAuthenticationServiseImpl INSTANCE;
    private List<User> users = List.of(
            new User("Борис", "Boris", "111"),
            new User("Анна", "Anna", "222"),
            new User("Дима", "Dima", "333"),
            new User("Коля", "Kolya", "444")
    );
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
}

