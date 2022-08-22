package components;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DBConnection {

    private final String url = "jdbc:sqlite:src/main/resources/db/users.db";
    @Getter
    private Connection connection;
    private Logger systemLogger;

    public DBConnection()  {
        this.systemLogger = ProjectLogger.getInstance().getSystemLogger();

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            systemLogger.error(ProjectLogger.stackTraceToString(e));
        }
    }


    public void closeConnectionToDb() throws SQLException {
        connection.close();
    }



}