package net.utory.rankpoint.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.utory.rankpoint.data.database.Database;

public final class DatabaseManager {

    private final ExecutorService executor;
    private final Connection conn;
    private final Database database;
    private final PreparedStatement selectPoint;
    private final PreparedStatement insertPoint;

    public DatabaseManager(Database database) throws SQLException {
        this.database = database;
        this.executor = Executors.newSingleThreadExecutor();
        this.conn = database.getConnection();
        database.initTable(conn);
        this.selectPoint = database.getSelectStatement(conn);
        this.insertPoint = database.getInsertStatement(conn);
    }

    public void loadPoint(UUID uuid, Consumer<Integer> consumer) {
        executor.execute(() -> {
            try {
                selectPoint.setString(1, uuid.toString());
                ResultSet rs = selectPoint.executeQuery();
                int r = 0;
                if (rs.next()) {
                    r = rs.getInt(1);
                }
                rs.close();
                consumer.accept(r);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadAllPoints(Consumer<Map<UUID, Integer>> consumer) {
        executor.execute(() -> {
            Map<UUID, Integer> map = new HashMap<>();
            try (Statement statement = conn.createStatement();
                ResultSet resultSet =
                    statement.executeQuery("SELECT * FROM " + database.getTableName())) {
                while (resultSet.next()) {
                    map.put(UUID.fromString(resultSet.getString(1)), resultSet.getInt(2));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            consumer.accept(map);
        });
    }

    public void savePoint(Map<UUID, Integer> points) {
        executor.execute(() -> {
            try {
                for (Map.Entry<UUID, Integer> entry : points.entrySet()) {
                    insertPoint.setString(1, entry.getKey().toString());
                    insertPoint.setInt(2, entry.getValue());
                    if (insertPoint.getParameterMetaData().getParameterCount() == 3) {
                        insertPoint.setInt(3, entry.getValue());
                    }
                    insertPoint.addBatch();
                }
                insertPoint.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void closeDatabase() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
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
