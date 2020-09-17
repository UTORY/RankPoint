package net.teamuni.rankpoint;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.teamuni.rankpoint.data.PlayerDataManager;
import net.teamuni.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class RPCommandExecutor implements TabExecutor {

    private final Rankpoint instance;

    RPCommandExecutor(Rankpoint instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean senderIsPlayer = sender instanceof Player;
        boolean senderHasPerm = sender.hasPermission("rankpoint.admin");
        if (args.length >= 1) {
            switch (args[0]) {
                case "me":
                    if (senderIsPlayer) {
                        loadAndRun(autoCast(sender), (data) -> {
                            // TODO 자신의 포인트 출력
                        });
                    }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
        String[] args) {
        return null;
    }

    private void loadAndRun(UUID uuid, Consumer<PlayerData> consumer) {
        PlayerDataManager dataManager = instance.getPlayerDataManager();
        if (dataManager.isLoaded(uuid)) {
            consumer.accept(dataManager.getPlayerData(uuid));
        } else {
            dataManager.loadPlayerData(uuid);
            consumer.accept(dataManager.getPlayerData(uuid));
            dataManager.unloadPlayerData(uuid);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T autoCast(Object object) {
        return (T) object;
    }
}
