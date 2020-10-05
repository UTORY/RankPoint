package net.utory.rankpoint.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.utory.rankpoint.data.database.Database;

public final class DatabaseManager {

    private final ExecutorService executor;
    private final Connection conn;
    private final PreparedStatement selectPoint;
    private final PreparedStatement insertPoint;

    public DatabaseManager(Database database) throws SQLException {
        this.executor = Executors.newSingleThreadExecutor();
        this.conn = database.getConnection();
        database.initTable(conn);
        this.selectPoint = database.getSelectStatement(conn);
        this.insertPoint = database.getInsertStatement(conn);
    }

    public int loadPoint(UUID uuid) {
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

    public void savePoint(UUID uuid, int point) {
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

    public void closeDatabase() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
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
