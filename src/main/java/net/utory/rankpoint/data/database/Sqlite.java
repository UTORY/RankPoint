package net.utory.rankpoint.data.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class Sqlite implements Database {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final String tableName;
    private final String dbFile;

    public Sqlite(String tableName, File dbFile) {
        this.tableName = tableName;
        this.dbFile = dbFile.getPath();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
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
        return conn.prepareStatement(
            "INSERT OR REPLACE INTO " + tableName + " (UUID, Point) VALUES (?, ?)");
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
