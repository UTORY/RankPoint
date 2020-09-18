package net.teamuni.rankpoint;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.teamuni.rankpoint.data.PlayerDataManager;
import net.teamuni.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.Bukkit;
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
        Message msg = instance.getMessage();
        if (args.length >= 1) {
            switch (args[0]) {
                case "me":
                    if (senderIsPlayer) {
                        loadAndRun(autoCast(sender), (data) -> {
                            sender.sendMessage(
                                String.format(msg.getMsg("command.me"), data.getPoint()));
                        });
                    }
                    break;
                case "look":
                    if (args.length == 2) {
                        //Arrays.stream(Bukkit.getOfflinePlayers()).anyMatch()
                        // TODO 주어진 플레이어가 한번이라도 서버에 접속 하지 않았다면
                        // TODO 막고, 접속 했었다면 데이터 가져와서 출력
                    } else {
                        sender.sendMessage(msg.getMsg("command.help.look"));
                    }
                    break;
                // TODO 다른 인자들도 구현하기
                default:
                    sender.sendMessage(msg.getMsg("command.help.me"));
                    sender.sendMessage(msg.getMsg("command.help.look"));
                    if (senderHasPerm) {
                        sender.sendMessage(msg.getMsg("command.help.give"));
                        sender.sendMessage(msg.getMsg("command.help.giveall"));
                        sender.sendMessage(msg.getMsg("command.help.take"));
                        sender.sendMessage(msg.getMsg("command.help.set"));
                        sender.sendMessage(msg.getMsg("command.help.reset"));
                        sender.sendMessage(msg.getMsg("command.help.reload"));
                    }
                    break;
            }
        }
        return true;
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
