package net.teamuni.rankpoint.data;

import java.util.HashMap;
import java.util.Map;
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
            loadPoint(uuid);
        }
        return playerDataMap.get(uuid);
    }

    public void unloadPlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        savePoint(uuid, playerDataMap.remove(uuid).getPoint());
    }

    public void close() {
        closeDatabase();
    }

    public static class PlayerData {

        private int point;

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
        }

        public void addPoint(int point) {
            if (point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + point + ")");
            }
            this.point += point;
        }

        public void removePoint(int point) {
            if (this.point - point < 0) {
                throw new IllegalArgumentException(
                    "point cannot be less than 0 (point: " + (this.point - point) + ")");
            }
            this.point -= point;
        }
    }
}
