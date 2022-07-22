package components;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DBConnection {

    private final String url = "jdbc:sqlite:src/main/resources/db/users.db";
    @Getter
    private final Connection connection;

    public DBConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection(url);
    }


    public void closeConnectionToDb() throws SQLException {
        connection.close();
    }



}