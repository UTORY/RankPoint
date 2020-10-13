package net.utory.rankpoint;

import java.util.Collections;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import net.utory.rankpoint.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public void updatePlayerRank(Player player) {
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
        if (point == 0) {
            return 0;
        }
        int n = -1;
        for (int cond : this.pointConditions) {
            if (cond != 0 && point < cond) {
                return n;
            }
            n++;
        }
        return n;
    }

    public int getPrretyPoint(int point) {
        if (point == 0 || point < pointConditions.get(0)) {
            return point;
        }
        return point - pointConditions.get(findGroup(point));
    }
}
