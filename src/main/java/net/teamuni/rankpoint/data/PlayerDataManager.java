package net.teamuni.rankpoint.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.scene.Group;
import net.teamuni.rankpoint.GroupConfig;
import net.teamuni.rankpoint.Rankpoint;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    protected abstract int loadPoint(UUID uuid);

    protected abstract void savePoint(UUID uuid, int point);

    protected abstract void closeDatabase();

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
        for (UUID uuid : playerDataMap.keySet()) {
            unloadPlayerData(uuid);
        }
    }

    public static final class PlayerData {

        private final Rankpoint instance = Rankpoint.getPlugin(Rankpoint.class);
        private final UUID uuid;
        private int point;
        private boolean isChanged = false;

        private PlayerData(UUID uuid, int point) {
            this.uuid = uuid;
            this.point = point;
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
            checkRank(oldPoint);
        }

        private void checkRank(int oldPoint) {
            GroupConfig groupConfig = instance.getGroupConfig();

            if (groupConfig.findGroup(oldPoint) != groupConfig.findGroup(point)) {
                groupConfig.updatePlayerRank(uuid);
            }
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

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            PlayerDataManager playerDataManager = instance.getPlayerDataManager();
            playerDataManager.unloadPlayerData(event.getPlayer().getUniqueId());
        }
    }
}
