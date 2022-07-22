package components;

import lombok.Getter;

import java.sql.*;

public class DBConnection {

    private final String url = "jdbc:sqlite:/home/boris/geekbrains/NetworkChat_server/src/main/resources/users.db";
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