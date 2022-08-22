package servises.impl;

import components.DBConnection;
import components.ProjectLogger;
import models.User;
import org.apache.log4j.Logger;
import servises.AuthenticationService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataBaseAuthServiceImp implements AuthenticationService {

    private static DataBaseAuthServiceImp INSTANCE;
    private final Logger systemLogger;

    DBConnection dbConnection;

    private DataBaseAuthServiceImp()  {
        dbConnection = new DBConnection();
        this.systemLogger = ProjectLogger.getInstance().getSystemLogger();
    }

    public static synchronized DataBaseAuthServiceImp getInstance()  {
        if (INSTANCE == null) {
            INSTANCE = new DataBaseAuthServiceImp();
        }
        return INSTANCE;
    }

    @Override
    public void changeNameByLogin(String userLogin, String newName) {
        String query = "UPDATE users SET name = '%s' WHERE login = '%s';";
        try {
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(String.format(query, newName, userLogin));
            stmt.executeUpdate();
            systemLogger.debug("ChangeNameByLogin " + stmt.toString());

        } catch (SQLException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }

    }

    @Override
    public String getNameByLogin(String login) {
        String query = "SELECT * FROM users WHERE login = '%s'";
        PreparedStatement stmt = null;
        String result = null;
        try {
            stmt = dbConnection.getConnection().prepareStatement(String.format(query, login));
            ResultSet resultSet = stmt.executeQuery();
            systemLogger.debug("getNameByLogin " + stmt.toString());

            while (resultSet.next()) {
                result = resultSet.getString("name");
            }
        } catch (SQLException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }
        return result;
    }


    @Override
    public String geNameByLoginAndPassword(String login, String password) {

        String result = null;
        try {
            String query = "SELECT * FROM users WHERE login = '%s' AND password = '%s';";
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(String.format( query,login,password));
            ResultSet resultSet = stmt.executeQuery();
            systemLogger.debug("geNameByLoginAndPassword " + stmt.toString());


            if (resultSet.next()) {
                return resultSet.getString("login");
            }
            } catch (SQLException e) {
                systemLogger.error(ProjectLogger.stackTraceToString(e));
            }
        return result;
    }

    @Override
    public List<String> getLogins() {
        ArrayList<String> result = new ArrayList<>();
        for (User user : getAllUsers()) {
            result.add(user.getLogin());
        }

        return result;
    }



    @Override
    public void addUser(String name, String login, String pass) {
        String query = "INSERT INTO users(id,login,password,name) VALUES(?,?,?,?)";

        try {
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, getLogins().size() + 1);
            stmt.setString(2, login);
            stmt.setString(3, pass);
            stmt.setString(4, name);
            stmt.executeUpdate();
            systemLogger.debug("addUser " + stmt.toString());

        } catch (SQLException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }
    }

    public List<User> getAllUsers()  {
        ArrayList<User> users = new ArrayList<>();

        try {
            String query = "SELECT * FROM users";
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();
            systemLogger.debug("getAllUsers " + stmt.toString());

            while (resultSet.next()) {
                users.add(User.builder()
                        .login(resultSet.getString("login"))
                        .password(resultSet.getString("password"))
                        .name(resultSet.getString("name"))
                        .build());
            }
        } catch (SQLException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }
        return users;
    }



}


