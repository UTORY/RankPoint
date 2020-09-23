package net.teamuni.rankpoint;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import net.teamuni.rankpoint.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class GroupConfig {

    private final Rankpoint instance;
    private final List<String> groupNames;
    private final List<Integer> pointConditions;

    GroupConfig(Rankpoint instance, List<String> groupNames, List<Integer> pointConditions) {
        this.instance = instance;
        this.groupNames = Collections.unmodifiableList(groupNames);
        this.pointConditions = Collections.unmodifiableList(pointConditions);
        if (this.groupNames.size() != this.pointConditions.size()) {
            throw new IllegalArgumentException(
                "The number of group names and the number of point conditions cannot be different. group names size: "
                    + groupNames + " point conditions size: " + pointConditions);
        }
    }

    public void updatePlayerRank(UUID uuid) {
        updatePlayerRank(Bukkit.getOfflinePlayer(uuid));
    }

    public void updatePlayerRank(OfflinePlayer player) {
        PlayerDataManager playerDataManager = instance.getPlayerDataManager();
        Permission permission = instance.getPermission();

        int point = playerDataManager.getPlayerData(player.getUniqueId()).getPoint();
        String groupName = groupNames.get(findGroup(point));

        for (String s : permission.getPlayerGroups(null, player)) {
            if (groupNames.contains(s) && !s.equals(groupName)) {
                permission.playerRemoveGroup(null, player, s);
            }
        }

        if (!permission.playerInGroup(null, player, groupName)) {
            permission.playerAddGroup(null, player, groupName);
        }
    }

    public int findGroup(int point) {
        int n = 0;
        for (int cond : this.pointConditions) {
            if (point <= cond) {
                break;
            }
            n++;
        }
        return n;
    }
}
