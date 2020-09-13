package net.teamuni.rankpoint.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class SqliteDataManager extends PlayerDataManager {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final Connection conn;
    private final PreparedStatement selectPoint;
    private final PreparedStatement insertPoint;

    public SqliteDataManager(String tableName, File dbFile) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName
            + " (UUID CHAR(36) NOT NULL PRIMARY KEY, Point INT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        String sql1 = "SELECT Point FROM " + tableName + " WHERE UUID = ?";
        String sql2 = "INSERT OR REPLACE INTO " + tableName + " (UUID, Point) " +
            "VALUES (?, ?)";
        selectPoint = conn.prepareStatement(sql1);
        insertPoint = conn.prepareStatement(sql2);
    }

    @Override
    protected int loadPoint(UUID uuid) {
        try {
            selectPoint.setString(1, uuid.toString());
            ResultSet rs = selectPoint.executeQuery();
            int r = 0;
            if (rs.next()) {
                r = rs.getInt(1);
            }
            rs.close();
            return r;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void savePoint(UUID uuid, int point) {
        try {
            insertPoint.setString(1, uuid.toString());
            insertPoint.setInt(2, point);
            insertPoint.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void closeDatabase() {
        try {
            if (selectPoint != null && !selectPoint.isClosed()) {
                selectPoint.close();
            }
            if (insertPoint != null && !insertPoint.isClosed()) {
                insertPoint.close();
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
