package net.utory.rankpoint.placeholderapi;

import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.utory.rankpoint.Rankpoint;
import net.utory.rankpoint.data.PlayerDataManager;
import net.utory.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.entity.Player;

@SuppressWarnings("NullableProblems")
public class RankpointExpansion extends PlaceholderExpansion {

    private final Rankpoint instance;

    public RankpointExpansion(Rankpoint instance) {
        this.instance = instance;
    }

    @Override
    public String getIdentifier() {
        return "rankpoint";
    }

    @Override
    public String getAuthor() {
        return instance.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        return player == null ? null : requestUUID(player.getUniqueId(), identifier);
    }

    private String requestUUID(UUID uuid, String identifier) {
        PlayerDataManager playerDataManager = instance.getPlayerDataManager();

        switch (identifier) {
            case "needpoint": {
                PlayerData data = playerDataManager.getPlayerDataOrNull(uuid);
                return data == null ? null : data.getNeedPoint();
            }
            case "totalpoint": {
                PlayerData data = playerDataManager.getPlayerDataOrNull(uuid);
                return data == null ? null : data.getTotalPoint();
            }
            case "point": {
                PlayerData data = playerDataManager.getPlayerDataOrNull(uuid);
                return data == null ? null : data.getPrettyPoint() + "";
            }
            case "rank": {
                PlayerData data = playerDataManager.getPlayerDataOrNull(uuid);
                return data == null ? null : data.getGroup();
            }
        }
        return null;
    }
}
