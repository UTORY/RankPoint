package net.utory.rankpoint.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import net.utory.rankpoint.GroupConfig;
import net.utory.rankpoint.Rankpoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Map<UUID, List<Consumer<PlayerData>>> loadingTask = new HashMap<>();
    private final Rankpoint instance;

    public PlayerDataManager(Rankpoint instance) {
        this.instance = instance;
    }

    private void loadPlayerData(UUID uuid) {
        if (playerDataMap.containsKey(uuid) || loadingTask.containsKey(uuid)) {
            return;
        }

        DatabaseManager databaseManager = instance.getDatabaseManager();

        loadingTask.put(uuid, new ArrayList<>());
        databaseManager.loadPoint(uuid, (i) ->
            Bukkit.getScheduler().runTask(instance, () -> {
                PlayerData pd = new PlayerData(uuid, i);
                playerDataMap.put(uuid, pd);
                for (Consumer<PlayerData> consumer : loadingTask.remove(uuid)) {
                    consumer.accept(pd);
                }
            }));
    }

    public void usePlayerData(UUID uuid, Consumer<PlayerData> consumer) {
        if (playerDataMap.containsKey(uuid)) {
            consumer.accept(playerDataMap.get(uuid));
            return;
        }
        if (loadingTask.containsKey(uuid)) {
            loadingTask.get(uuid).add(consumer);
            return;
        }
        loadPlayerData(uuid);
        if (loadingTask.containsKey(uuid)) {
            loadingTask.get(uuid).add(consumer);
        } else if (playerDataMap.containsKey(uuid)) {
            consumer.accept(playerDataMap.get(uuid));
        } else {
            throw new RuntimeException("Faild to load player point.");
        }
    }

    public void saveAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }
        Map<UUID, Integer> points = new HashMap<>();

        for (Entry<UUID, PlayerData> entry : new HashMap<>(playerDataMap).entrySet()) {
            if (entry.getValue().isChanged) {
                points.put(entry.getKey(), entry.getValue().point);
            }
        }

        if (!points.isEmpty()) {
            DatabaseManager databaseManager = instance.getDatabaseManager();
            databaseManager.savePoint(points);
        }
    }

    public void close() {
        unloadAllData();
        DatabaseManager databaseManager = instance.getDatabaseManager();
        databaseManager.closeDatabase();
    }

    public void allPlayerDataLoad(Collection<? extends Player> players) {
        for (Player player : players) {
            loadPlayerData(player.getUniqueId());
        }
    }

    private void unloadAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }

        Map<UUID, Integer> willSaveData = new HashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            if (entry.getValue().isChanged) {
                willSaveData.put(entry.getKey(), entry.getValue().point);
            }
        }
        playerDataMap.clear();
        if (willSaveData.isEmpty()) {
            return;
        }

        DatabaseManager databaseManager = instance.getDatabaseManager();
        databaseManager.savePoint(willSaveData);
    }

    public static final class PlayerData {

        private static final Rankpoint instance = Rankpoint.getPlugin(Rankpoint.class);

        private final UUID uuid;
        private int point;
        private int prettyPoint;
        private boolean isChanged = false;

        private PlayerData(UUID uuid, int point) {
            this.uuid = uuid;
            this.point = point;
            updatePrettyPoint();
        }

        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            if (point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + point + ")");
            }
            int oldPoint = this.point;
            this.point = point;
            isChanged = true;
            updatePrettyPoint();
            checkRank(oldPoint);
        }

        public void addPoint(int point) {
            if (point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + point + ")");
            }
            int oldPoint = this.point;
            this.point += point;
            isChanged = true;
            updatePrettyPoint();
            checkRank(oldPoint);
        }

        public void removePoint(int point) {
            if (this.point - point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + (this.point - point) + ")");
            }
            int oldPoint = this.point;
            this.point -= point;
            isChanged = true;
            updatePrettyPoint();
            checkRank(oldPoint);
        }

        public int getPrettyPoint() {
            return prettyPoint;
        }

        private void checkRank(int oldPoint) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) {
                return;
            }

            GroupConfig groupConfig = instance.getGroupConfig();

            if (groupConfig.findGroup(oldPoint) != groupConfig.findGroup(point)) {
                groupConfig.updatePlayerRank(p, point);
            }
        }

        private void updatePrettyPoint() {
            GroupConfig groupConfig = instance.getGroupConfig();

            prettyPoint = groupConfig.getPrretyPoint(point);
        }
    }

    public static final class PlayerListener implements Listener {

        private final Rankpoint instance;

        public PlayerListener(Rankpoint instance) {
            this.instance = instance;
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            PlayerDataManager playerDataManager = instance.getPlayerDataManager();
            playerDataManager.loadPlayerData(event.getPlayer().getUniqueId());
        }
    }
}
