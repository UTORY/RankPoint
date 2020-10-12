package net.utory.rankpoint.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.utory.rankpoint.GroupConfig;
import net.utory.rankpoint.Rankpoint;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Rankpoint instance;

    public PlayerDataManager(Rankpoint instance) {
        this.instance = instance;
    }

    public void loadPlayerData(UUID uuid) {
        playerDataMap.put(uuid, new PlayerData(uuid, loadPoint(uuid)));
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            loadPlayerData(uuid);
        }
        return playerDataMap.get(uuid);
    }

    public boolean isLoaded(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public void unloadPlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        PlayerData data = playerDataMap.remove(uuid);
        if (data.isChanged) {
            savePoint(uuid, data.getPoint());
        }
    }

    public void saveAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }
        for (Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            if (entry.getValue().isChanged) {
                savePoint(entry.getKey(), entry.getValue().getPoint());
            }
        }
    }

    public void close() {
        unloadAllData();
        closeDatabase();
    }

    private void unloadAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }
        HashMap<UUID, PlayerData> newPdm = new HashMap<>(playerDataMap);
        for (UUID uuid : newPdm.keySet()) {
            unloadPlayerData(uuid);
        }
    }

    private int loadPoint(UUID uuid) {
        DatabaseManager databaseManager = instance.getDatabaseManager();
        return databaseManager.loadPoint(uuid);
    }

    private void savePoint(UUID uuid, int point) {
        DatabaseManager databaseManager = instance.getDatabaseManager();
        databaseManager.savePoint(uuid, point);
    }

    private void closeDatabase() {
        DatabaseManager databaseManager = instance.getDatabaseManager();
        databaseManager.closeDatabase();
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
            if (Bukkit.getPlayer(uuid) == null) {
                return;
            }

            GroupConfig groupConfig = instance.getGroupConfig();

            if (groupConfig.findGroup(oldPoint) != groupConfig.findGroup(point)) {
                groupConfig.updatePlayerRank(Bukkit.getPlayer(uuid));
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
            instance.getGroupConfig().updatePlayerRank(event.getPlayer());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            PlayerDataManager playerDataManager = instance.getPlayerDataManager();
            playerDataManager.unloadPlayerData(event.getPlayer().getUniqueId());
        }
    }
}
