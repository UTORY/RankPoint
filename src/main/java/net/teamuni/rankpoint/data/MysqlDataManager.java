package net.teamuni.rankpoint.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlDataManager extends PlayerDataManager {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final ExecutorService executor;
    private final Connection conn;
    private final PreparedStatement selectPoint;
    private final PreparedStatement insertPoint;

    public MysqlDataManager(String hostName, int port, String database, String parameters,
        String tableName, String userName, String password) throws SQLException {
        executor = Executors.newSingleThreadExecutor();
        String url = "jdbc:mysql://" + hostName + ":" + port + "/" + database + parameters;
        conn = DriverManager.getConnection(url, userName, password);
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName
            + " (UUID CHAR(36) NOT NULL PRIMARY KEY, Point INT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        String sql1 = "SELECT Point FROM " + tableName + " WHERE UUID = ?";
        String sql2 = "INSERT INTO " + tableName + " (UUID, Point) VALUES (?, ?) ON DUPLICATE KEY UPDATE Point = ?";
        selectPoint = conn.prepareStatement(sql1);
        insertPoint = conn.prepareStatement(sql2);
    }

    @Override
    protected int loadPoint(UUID uuid) {
        try {
            return executor.submit(() -> {
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
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void savePoint(UUID uuid, int point) {
        executor.execute(() -> {
            try {
                insertPoint.setString(1, uuid.toString());
                insertPoint.setInt(2, point);
                insertPoint.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void closeDatabase() {
        executor.shutdown();
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
