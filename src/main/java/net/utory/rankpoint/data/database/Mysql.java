package net.utory.rankpoint.data.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Mysql implements Database {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final String hostName;
    private final int port;
    private final String database;
    private final String parameters;
    private final String tableName;
    private final String userName;
    private final String password;

    public Mysql(String hostName, int port, String database, String parameters, String tableName,
        String userName, String password) {
        this.hostName = hostName;
        this.port = port;
        this.database = database;
        this.parameters = parameters;
        this.tableName = tableName;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager
            .getConnection(("jdbc:mysql://" + hostName + ":" + port + "/" + database + parameters),
                userName, password);
    }

    @Override
    public void initTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName
                + " (UUID CHAR(36) NOT NULL PRIMARY KEY, Point INT NOT NULL)");
        }
    }

    @Override
    public PreparedStatement getSelectStatement(Connection conn) throws SQLException {
        return conn.prepareStatement("SELECT Point FROM " + tableName + " WHERE UUID = ?");
    }

    @Override
    public PreparedStatement getInsertStatement(Connection conn) throws SQLException {
        return conn.prepareStatement("INSERT INTO " + tableName
            + " (UUID, Point) VALUES (?, ?) ON DUPLICATE KEY UPDATE Point = ?");
    }
}
