package net.teamuni.rankpoint.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public abstract class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    protected abstract int loadPoint(UUID uuid);

    protected abstract void savePoint(UUID uuid, int point);

    protected abstract void closeDatabase();

    public void loadPlayerData(UUID uuid) {
        playerDataMap.put(uuid, new PlayerData(loadPoint(uuid)));
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
        if(data.isChanged) {
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

    public static class PlayerData {

        private int point;
        private boolean isChanged = false;

        private PlayerData(int point) {
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
            this.point = point;
            isChanged = true;
        }

        public void addPoint(int point) {
            if (point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + point + ")");
            }
            this.point += point;
            isChanged = true;
        }

        public void removePoint(int point) {
            if (this.point - point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + (this.point - point) + ")");
            }
            this.point -= point;
            isChanged = true;
        }
    }
}
