package net.utory.rankpoint;

import net.utory.rankpoint.data.PlayerDataManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class Message {

    private String BROADCAST_RANKUP;
    private String COMMAND_DONTHAVEPERM;
    private String COMMAND_UNKNOWNARG;
    private String COMMAND_HELP_ME;
    private String COMMAND_HELP_LOOK;
    private String COMMAND_HELP_GIVE;
    private String COMMAND_HELP_GIVEALL;
    private String COMMAND_HELP_TAKE;
    private String COMMAND_HELP_SET;
    private String COMMAND_HELP_RESET;
    private String COMMAND_HELP_RELOAD;
    private String COMMAND_ME;
    private String COMMAND_LOOK;
    private String COMMAND_GIVE_SENDER;
    private String COMMAND_GIVE_RECEIVER;
    private String COMMAND_GIVEALL;
    private String COMMAND_TAKE_SENDER;
    private String COMMAND_TAKE_RECEIVER;
    private String COMMAND_SET_SENDER;
    private String COMMAND_SET_RECEIVER;
    private String COMMAND_RESET_SENDER;
    private String COMMAND_RESET_RECEIVER;
    private String COMMAND_RELOAD_SUCCESS;
    private String COMMAND_RELOAD_FAILED;

    public static void sendMessage(CommandSender sender, String str, String senderName, String receiver, PlayerData receiverData, String arg) {
        for (String s : format(str, senderName, receiver, receiverData, arg).split("\\\\n")) {
            sender.sendMessage(s);
        }
    }
    public static void broadcastMessage(String str, String senderName, String receiver, PlayerData receiverData, String arg) {
        for (String s : format(str, senderName, receiver, receiverData, arg).split("\\\\n")) {
            Bukkit.broadcastMessage(s);
        }
    }

    private static String format(String str, String senderName, String receiver, PlayerData receiverData, String arg) {
        if (senderName != null) {
            str = str.replace("<sender>", senderName);
        }
        if (receiver != null) {
            str = str.replace("<receiver>", receiver);
        }
        if (receiverData != null) {
            str = str.replace("<point>", receiverData.getPrettyPoint()+"")
                .replace("<need_point>", receiverData.getNeedPoint()+"")
                .replace("<total_point>", receiverData.getTotalPoint()+"")
                .replace("<rank>", receiverData.getGroup());
        }
        if (arg != null) {
            str = str.replace("<arg>", arg);
        }
        return str;
    }

    public static void sendMessage(CommandSender sender, String str) {
        sendMessage(sender, str, null, null, null, null);
    }

    public void loadMessages(FileConfiguration conf) {
        BROADCAST_RANKUP = getString(conf, "broadcast.rankup");
        COMMAND_DONTHAVEPERM = getString(conf, "command.donthaveperm");
        COMMAND_UNKNOWNARG = getString(conf, "command.unknownarg");
        COMMAND_HELP_ME = getString(conf, "command.help.me");
        COMMAND_HELP_LOOK = getString(conf, "command.help.look");
        COMMAND_HELP_GIVE = getString(conf, "command.help.give");
        COMMAND_HELP_GIVEALL = getString(conf, "command.help.giveall");
        COMMAND_HELP_TAKE = getString(conf, "command.help.take");
        COMMAND_HELP_SET = getString(conf, "command.help.set");
        COMMAND_HELP_RESET = getString(conf, "command.help.reset");
        COMMAND_HELP_RELOAD = getString(conf, "command.help.reload");
        COMMAND_ME = getString(conf, "command.me");
        COMMAND_LOOK = getString(conf, "command.look");
        COMMAND_GIVE_SENDER = getString(conf, "command.give.sender");
        COMMAND_GIVE_RECEIVER = getString(conf, "command.give.receiver");
        COMMAND_GIVEALL = getString(conf, "command.giveall");
        COMMAND_TAKE_SENDER = getString(conf, "command.take.sender");
        COMMAND_TAKE_RECEIVER = getString(conf, "command.take.receiver");
        COMMAND_SET_SENDER = getString(conf, "command.set.sender");
        COMMAND_SET_RECEIVER = getString(conf, "command.set.receiver");
        COMMAND_RESET_SENDER = getString(conf, "command.reset.sender");
        COMMAND_RESET_RECEIVER = getString(conf, "command.reset.receiver");
        COMMAND_RELOAD_SUCCESS = getString(conf, "command.reload.success");
        COMMAND_RELOAD_FAILED = getString(conf, "command.reload.failed");
    }

    private String getString(FileConfiguration conf, String path) {
        if (conf.isList(path)) {
            return ChatColor
                .translateAlternateColorCodes('&', String.join("\\n", conf.getStringList(path)));
        } else {
            return ChatColor.translateAlternateColorCodes('&', conf.getString(path, path));
        }
    }

    public String BroadcastRankup() {
        return BROADCAST_RANKUP;
    }

    public String CommandDonthaveperm() {
        return COMMAND_DONTHAVEPERM;
    }

    public String CommandUnknownarg() {
        return COMMAND_UNKNOWNARG;
    }

    public String CommandHelpMe() {
        return COMMAND_HELP_ME;
    }

    public String CommandHelpLook() {
        return COMMAND_HELP_LOOK;
    }

    public String CommandHelpGive() {
        return COMMAND_HELP_GIVE;
    }

    public String CommandHelpGiveall() {
        return COMMAND_HELP_GIVEALL;
    }

    public String CommandHelpTake() {
        return COMMAND_HELP_TAKE;
    }

    public String CommandHelpSet() {
        return COMMAND_HELP_SET;
    }

    public String CommandHelpReset() {
        return COMMAND_HELP_RESET;
    }

    public String CommandHelpReload() {
        return COMMAND_HELP_RELOAD;
    }

    public String CommandMe() {
        return COMMAND_ME;
    }

    public String CommandLook() {
        return COMMAND_LOOK;
    }

    public String CommandGiveSender() {
        return COMMAND_GIVE_SENDER;
    }

    public String CommandGiveReceiver() {
        return COMMAND_GIVE_RECEIVER;
    }

    public String CommandGiveall() {
        return COMMAND_GIVEALL;
    }

    public String CommandTakeSender() {
        return COMMAND_TAKE_SENDER;
    }

    public String CommandTakeReceiver() {
        return COMMAND_TAKE_RECEIVER;
    }

    public String CommandSetSender() {
        return COMMAND_SET_SENDER;
    }

    public String CommandSetReceiver() {
        return COMMAND_SET_RECEIVER;
    }

    public String CommandResetSender() {
        return COMMAND_RESET_SENDER;
    }

    public String CommandResetReceiver() {
        return COMMAND_RESET_RECEIVER;
    }

    public String CommandReloadSuccess() {
        return COMMAND_RELOAD_SUCCESS;
    }

    public String CommandReloadFailed() {
        return COMMAND_RELOAD_FAILED;
    }
}
