package servises;

import models.User;

import java.util.List;

public interface AuthenticationService {
    String geNameByLoginAndPassword(String login, String password);

    List<String> getLogins();

    void addUser(String name, String login, String pass);

    String getNameByLogin(String login);

    void changeNameByLogin(String userLogin, String newName);

}
