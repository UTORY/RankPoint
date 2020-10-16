package net.utory.rankpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.utory.rankpoint.data.PlayerDataManager;
import net.utory.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class RPCommandExecutor implements TabExecutor {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    private final Rankpoint instance;

    RPCommandExecutor(Rankpoint instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean senderIsPlayer = sender instanceof Player;
        boolean senderHasPerm = sender.hasPermission("rankpoint.admin");
        Message msg = instance.getMessage();
        if (args.length >= 1 && !args[0].equals("help")) {
            switch (args[0].toLowerCase()) {
                case "me":
                    if (senderIsPlayer) {
                        loadAndRun(((Player) sender).getUniqueId(),
                            data -> sender
                                .sendMessage(msg.getMsg("command.me", data.getPrettyPoint())));
                    }
                    break;
                case "look":
                    if (args.length == 2 && checkPlayerName(args[1])) {
                        loadAndRun(args[1], (data, player) -> sender
                            .sendMessage(
                                msg.getMsg("command.look", player.getName(),
                                    data.getPrettyPoint())));
                        return true;
                    }
                    sender.sendMessage(msg.getMsg("command.help.look"));
                    break;
                case "give":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && isIntegerAndPositive(
                            args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                data.addPoint(point);
                                sender.sendMessage(
                                    msg.getMsg("command.give.sender", player.getName(), point));
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    ((Player) player).sendMessage(
                                        msg.getMsg("command.give.receiver", sender.getName(),
                                            point));
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(msg.getMsg("command.help.give"));
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                case "giveall":
                    if (senderHasPerm) {
                        if (args.length == 2 && isIntegerAndPositive(args[1])) {
                            int point = Integer.parseInt(args[1]);
                            Bukkit.getOnlinePlayers().forEach(
                                (player) -> loadAndRun(player.getUniqueId(),
                                    (data) -> data.addPoint(point)));
                            Bukkit.broadcastMessage(msg.getMsg("command.giveall", point));
                            return true;
                        }
                        sender.sendMessage(msg.getMsg("command.help.giveall"));
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                case "take":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && isIntegerAndPositive(
                            args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                if (data.getPoint() - point < 0) {
                                    data.setPoint(0);
                                } else {
                                    data.removePoint(point);
                                }
                                sender.sendMessage(
                                    msg.getMsg("command.take.sender", player.getName(), point));
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    ((Player) player).sendMessage(
                                        msg.getMsg("command.take.receiver", sender.getName(),
                                            point));
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(msg.getMsg("command.help.take"));
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                case "set":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && isIntegerAndPositive(
                            args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                data.setPoint(point);
                                sender.sendMessage(
                                    msg.getMsg("command.set.sender", player.getName(), point));
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    ((Player) player).sendMessage(
                                        msg.getMsg("command.set.receiver", sender.getName(),
                                            point));
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(msg.getMsg("command.help.set"));
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                case "reset":
                    if (senderHasPerm) {
                        if (args.length == 2 && checkPlayerName(args[1])) {
                            loadAndRun(args[1], (data, player) -> {
                                data.setPoint(0);
                                sender.sendMessage(
                                    msg.getMsg("command.reset.sender", player.getName()));
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    ((Player) player).sendMessage(
                                        msg.getMsg("command.reset.receiver", sender.getName()));
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(msg.getMsg("command.help.reset"));
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                case "reload":
                    if (senderHasPerm) {
                        if (instance.configReload()) {
                            sender.sendMessage(msg.getMsg("command.reload.success"));
                        } else {
                            sender.sendMessage(msg.getMsg("command.reload.failed"));
                        }
                    } else {
                        sender.sendMessage(msg.getMsg("command.donthaveperm"));
                    }
                    break;
                default:
                    sender.sendMessage(msg.getMsg("command.unknownarg"));
                    break;
            }
        } else {
            if (senderIsPlayer) {
                sender.sendMessage(msg.getMsg("command.help.me"));
            }
            sender.sendMessage(msg.getMsg("command.help.look"));
            if (senderHasPerm) {
                sender.sendMessage(msg.getMsg("command.help.give"));
                sender.sendMessage(msg.getMsg("command.help.giveall"));
                sender.sendMessage(msg.getMsg("command.help.take"));
                sender.sendMessage(msg.getMsg("command.help.set"));
                sender.sendMessage(msg.getMsg("command.help.reset"));
                sender.sendMessage(msg.getMsg("command.help.reload"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
        String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 0 || args[0].isEmpty()) {
            if (sender instanceof Player) {
                list.add("me");
            }
            list.add("look");
            if (sender.hasPermission("rankpoint.admin")) {
                list.add("give");
                list.add("giveall");
                list.add("take");
                list.add("set");
                list.add("reset");
                list.add("reload");
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "look":
                    list = null;
                    break;
                case "give":
                case "take":
                case "set":
                case "reset":
                    if (sender.hasPermission("rankpoint.admin")) {
                        list = null;
                    }
                    break;
                default:
                    break;
            }
        }
        return list;
    }

    private boolean checkPlayerName(String name) {
        return USERNAME_PATTERN.matcher(name).matches();
    }

    private boolean isIntegerAndPositive(String st) {
        try {
            int i = Integer.parseInt(st);
            if (i > 0) {
                return true;
            }
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    private void loadAndRun(UUID uuid, Consumer<PlayerData> consumer) {
        PlayerDataManager dataManager = instance.getPlayerDataManager();
        dataManager.usePlayerData(uuid, consumer);
    }

    @SuppressWarnings("deprecation")
    private void loadAndRun(String name, BiConsumer<PlayerData, OfflinePlayer> consumer) {
        PlayerDataManager dataManager = instance.getPlayerDataManager();
        Player p = Bukkit.getPlayerExact(name);
        if (p != null) {
            dataManager.usePlayerData(p.getUniqueId(), (data) -> {
                consumer.accept(data, p);
            });
        } else {
            CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(name))
                .thenAccept(player -> Bukkit.getScheduler().runTask(instance,
                    () -> dataManager.usePlayerData(player.getUniqueId(),
                        (data) -> consumer.accept(data, player))));
        }
    }
}
