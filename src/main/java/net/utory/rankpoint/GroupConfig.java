package net.utory.rankpoint;

import static net.utory.rankpoint.Message.broadcastMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.permission.Permission;
import net.utory.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.entity.Player;

public final class GroupConfig {

    private final Rankpoint instance;
    private final List<String> groupNames;
    private final List<Integer> pointConditions;
    private final Map<String, String> displayGroupNamesMap;

    GroupConfig(Rankpoint instance, List<String> groupNames, List<Integer> pointConditions,
        Map<String, String> displayGroupNamesMap) {
        this.instance = instance;
        this.groupNames = Collections.unmodifiableList(groupNames);
        this.pointConditions = Collections.unmodifiableList(pointConditions);
        this.displayGroupNamesMap = displayGroupNamesMap;
        if (this.groupNames.size() != this.pointConditions.size()) {
            throw new IllegalArgumentException(
                "The number of group names and the number of point conditions cannot be different. group names size: "
                    + groupNames + " point conditions size: " + pointConditions);
        }
    }

    public void updatePlayerRank(Player player, PlayerData playerData) {
        if (!player.isOnline()) {
            return;
        }

        Permission permission = instance.getPermission();
        String groupName = groupNames.get(playerData.getGroupInt());

        for (String s : permission.getPlayerGroups(null, player)) {
            if (groupNames.contains(s) && !s.equals(groupName)) {
                permission.playerRemoveGroup(null, player, s);
            }
        }

        if (!permission.playerInGroup(null, player, groupName)) {
            permission.playerAddGroup(null, player, groupName);
            Message msg = instance.getMessage();
            broadcastMessage(msg.BroadcastRankup(), null, player.getName(), playerData, null);
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

    public String getGroupName(int group) {
        return displayGroupNamesMap.get(groupNames.get(group));
    }

    public int getPrretyPoint(int point) {
        if (point == 0 || point < pointConditions.get(0)) {
            return point;
        }
        return point - pointConditions.get(findGroup(point));
    }

    public int getTotalPoint(int group) {
        return pointConditions.get(group + 1) - pointConditions.get(group);
    }

    public int getNeedPoint(int group, int point) {
        return pointConditions.get(group + 1) - point;
    }
}
