package net.teamuni.rankpoint;

import java.util.List;
import net.milkbowl.vault.permission.Permission;
import net.teamuni.rankpoint.data.PlayerDataManager;
import org.bukkit.OfflinePlayer;

public final class GroupConfig {

    private final Rankpoint instance;
    private final List<String> groupNames;
    private final List<Integer> pointConditions;

    GroupConfig(Rankpoint instance, List<String> groupNames, List<Integer> pointConditions) {
        this.instance = instance;
        this.groupNames = groupNames;
        this.pointConditions = pointConditions;
    }

    public void updatePlayerRank(OfflinePlayer player) {
        PlayerDataManager playerDataManager = instance.getPlayerDataManager();
        Permission permission = instance.getPermission();

        int point = playerDataManager.getPlayerData(player.getUniqueId()).getPoint();
        int n = 0;
        for (int i : pointConditions) {
            if (point <= i) {
                break;
            }
            n++;
        }

        String groupName;
        if (n == groupNames.size()) {
            groupName = groupNames.get(groupNames.size() - 1);
        } else {
            groupName = groupNames.get(n);
        }

        for (String s : permission.getPlayerGroups(null, player)) {
            if (groupNames.contains(s) && !s.equals(groupName)) {
                permission.playerRemoveGroup(null, player, s);
            }
        }

        if (!permission.playerInGroup(null, player, groupName)) {
            permission.playerAddGroup(null, player, groupName);
        }
    }
}
