package net.utory.rankpoint;

import static net.utory.rankpoint.Message.broadcastMessage;
import static net.utory.rankpoint.Message.sendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
                            data -> sendMessage(sender, msg.CommandMe(), sender.getName(),
                                sender.getName(), data, null));
                    }
                    break;
                case "look":
                    if (args.length == 2 && checkPlayerName(args[1])) {
                        loadAndRun(args[1], (data, player) ->
                            sendMessage(sender, msg.CommandLook(), sender.getName(),
                                player.getName(), data, null));
                        return true;
                    }
                    sendMessage(sender, msg.CommandHelpLook());
                    break;
                case "give":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && checkInt(args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                data.addPoint(point);
                                sendMessage(sender, msg.CommandGiveSender(), sender.getName(),
                                    player.getName(), data, args[2]);
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    sendMessage(((Player) player), msg.CommandGiveReceiver(),
                                        sender.getName(), player.getName(), data, args[2]);
                                }
                            });
                            return true;
                        }
                        sender.sendMessage(msg.CommandHelpGive());
                    } else {
                        sender.sendMessage(msg.CommandDonthaveperm());
                    }
                    break;
                case "giveall":
                    if (senderHasPerm) {
                        if (args.length == 2 && checkInt(args[1])) {
                            int point = Integer.parseInt(args[1]);
                            Bukkit.getOnlinePlayers().forEach(
                                (player) -> loadAndRun(player.getUniqueId(),
                                    (data) -> data.addPoint(point)));
                            broadcastMessage(msg.CommandGiveall(), sender.getName(), null, null,
                                point + "");
                            return true;
                        }
                        sendMessage(sender, msg.CommandHelpGiveall());
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                case "take":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && checkInt(args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                if (data.getPoint() - point < 0) {
                                    data.setPoint(0);
                                } else {
                                    data.removePoint(point);
                                }
                                sendMessage(sender, msg.CommandTakeSender(), sender.getName(),
                                    player.getName(), data, args[2]);
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    sendMessage(((Player) player), msg.CommandTakeReceiver(),
                                        sender.getName(), player.getName(), data, args[2]);
                                }
                            });
                            return true;
                        }
                        sendMessage(sender, msg.CommandHelpTake());
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                case "set":
                    if (senderHasPerm) {
                        if (args.length == 3 && checkPlayerName(args[1]) && checkInt(args[2])) {
                            int point = Integer.parseInt(args[2]);
                            loadAndRun(args[1], (data, player) -> {
                                data.setPoint(point);
                                sendMessage(sender, msg.CommandSetSender(), sender.getName(),
                                    player.getName(), data, args[2]);
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    sendMessage(((Player) player), msg.CommandSetReceiver(),
                                        sender.getName(), player.getName(), data, args[2]);
                                }
                            });
                            return true;
                        }
                        sendMessage(sender, msg.CommandHelpSet());
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                case "reset":
                    if (senderHasPerm) {
                        if (args.length == 2 && checkPlayerName(args[1])) {
                            loadAndRun(args[1], (data, player) -> {
                                data.setPoint(0);
                                sendMessage(sender, msg.CommandResetSender(), sender.getName(),
                                    player.getName(), data, null);
                                if (player.isOnline() && !sender.getName()
                                    .equals(player.getName())) {
                                    sendMessage(((Player) player), msg.CommandResetReceiver(),
                                        sender.getName(), player.getName(), data, null);
                                }
                            });
                            return true;
                        }
                        sendMessage(sender, msg.CommandHelpReset());
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                case "reload":
                    if (senderHasPerm) {
                        if (instance.configReload()) {
                            sendMessage(sender, msg.CommandReloadSuccess());
                        } else {
                            sendMessage(sender, msg.CommandReloadFailed());
                        }
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                case "migrate":
                    if (senderHasPerm) {
                        instance.migrate();
                        sendMessage(sender, msg.CommandMigrate());
                    } else {
                        sendMessage(sender, msg.CommandDonthaveperm());
                    }
                    break;
                default:
                    sendMessage(sender, msg.CommandUnknownarg());
                    break;
            }
        } else {
            if (senderIsPlayer) {
                sendMessage(sender, msg.CommandHelpMe());
            }
            sendMessage(sender, msg.CommandHelpLook());
            if (senderHasPerm) {
                sendMessage(sender, msg.CommandHelpGive());
                sendMessage(sender, msg.CommandHelpGiveall());
                sendMessage(sender, msg.CommandHelpTake());
                sendMessage(sender, msg.CommandHelpSet());
                sendMessage(sender, msg.CommandHelpReset());
                sendMessage(sender, msg.CommandHelpReload());
                sendMessage(sender, msg.CommandHelpMigrate());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
        String[] args) {
        if (args.length == 2) {
            switch (args[0]) {
                case "look":
                    return null;
                case "give":
                case "take":
                case "set":
                case "reset":
                    if (sender.hasPermission("rankpoint.admin")) {
                        return null;
                    }
                default:
                    break;
            }
        }

        if (args.length >= 2) {
            return new ArrayList<>();
        }

        ArrayList<String> list = new ArrayList<>();
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
            list.add("migrate");
        }
        if (args.length == 0 || args[0].isEmpty()) {
            return list;
        } else {
            return list.stream().filter(str -> str.startsWith(args[0]))
                .collect(Collectors.toList());
        }
    }

    private boolean checkPlayerName(String name) {
        return USERNAME_PATTERN.matcher(name).matches();
    }

    private boolean checkInt(String st) {
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
