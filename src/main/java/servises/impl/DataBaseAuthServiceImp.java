package servises.impl;

import components.DBConnection;
import servises.AuthenticationService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataBaseAuthServiceImp implements AuthenticationService {

    private static DataBaseAuthServiceImp INSTANCE;

    DBConnection dbConnection;

    @Override
    public String getNameByLogin(String login) {
        String query = "SELECT * FROM users WHERE login = '%s'";
        PreparedStatement stmt = null;
        String result = null;
        try {
            stmt = dbConnection.getConnection().prepareStatement(String.format(query, login));
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public DataBaseAuthServiceImp() throws SQLException, ClassNotFoundException {
        dbConnection = new DBConnection();
    }

    public static synchronized DataBaseAuthServiceImp getInstance() throws SQLException, ClassNotFoundException {
        if (INSTANCE == null) {
            INSTANCE = new DataBaseAuthServiceImp();
        }
        return INSTANCE;
    }


    @Override
    public String geNameByLoginAndPassword(String login, String password) {
        String result = null;

        try {
            ResultSet resultSet = getUsers();

            while (resultSet.next()) {
                if (resultSet.getString("login").equals(login) && resultSet.getString("password").equals(password)) {
                    return resultSet.getString("login");
                }
            }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        return result;
    }

    @Override
    public List<String> getLogins() {
        ArrayList<String> result = new ArrayList<>();

        ResultSet set = null;
        try {
            set = getUsers();
            while (set.next()) {
                result.add(set.getString("login"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getUsers() throws SQLException {
        String query = "SELECT * FROM users";
        PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query);
        return stmt.executeQuery();
    }



}


