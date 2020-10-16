package net.utory.rankpoint;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.milkbowl.vault.permission.Permission;
import net.utory.rankpoint.data.DatabaseManager;
import net.utory.rankpoint.data.PlayerDataManager;
import net.utory.rankpoint.data.PlayerDataManager.PlayerListener;
import net.utory.rankpoint.data.database.Mysql;
import net.utory.rankpoint.data.database.Sqlite;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Rankpoint extends JavaPlugin {

    private Permission perms;
    private PlayerDataManager playerDataManager;
    private DatabaseManager databaseManager;
    private Message message;
    private GroupConfig groupConfig;

    @Override
    public void onEnable() {
        if (!setupPermission()) {
            getLogger().severe("Vault-Permission 에 연결할 수 없습니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupConfig()) {
            getLogger().severe("Config 를 불러올 수 없습니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupDatabase()) {
            getLogger().severe("데이터베이스에 연결할 수 없습니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PluginCommand command = getCommand("rankpoint");
        RPCommandExecutor executor = new RPCommandExecutor(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public boolean configReload() {
        Bukkit.getScheduler().cancelTasks(this);
        playerDataManager.close();
        if (setupConfig() && setupDatabase()) {
            playerDataManager.allPlayerDataLoad(Bukkit.getOnlinePlayers());
            return true;
        }
        return false;
    }

    private boolean setupPermission() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager()
            .getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return true;
    }

    private boolean setupConfig() {
        saveDefaultConfig();
        reloadConfig();
        File msgConf = new File(getDataFolder(), "message.yml");
        if (!msgConf.exists()) {
            saveResource("message.yml", false);
        }
        message = new Message(YamlConfiguration.loadConfiguration(msgConf));
        ConfigurationSection groups = getConfig().getConfigurationSection("groups");
        if (groups == null) {
            getLogger().severe("config의 groups 설정을 불러오는데 실패했습니다.");
            return false;
        }
        List<String> allGroups = Arrays.asList(perms.getGroups());
        List<String> groupNames = new ArrayList<>();
        List<Integer> pointConditions = new ArrayList<>();
        // 람다에서는 final 변수만 접근 가능
        final int[] latest = {0};
        groups.getKeys(false).stream().mapToInt(Integer::parseInt).sorted()
            .mapToObj(String::valueOf).map(groups::getConfigurationSection).filter(Objects::nonNull)
            .forEach(section -> {
                String groupName = section.getString("group").toLowerCase();
                if (allGroups.contains(groupName)) {
                    groupNames.add(groupName);
                    latest[0] += section.getInt("point");
                    pointConditions.add(latest[0]);
                } else {
                    getLogger().severe(groupName + " 는(은) 없는 그룹입니다.");
                }
            });
        if (groupNames.isEmpty() || pointConditions.isEmpty()) {
            return false;
        }
        groupConfig = new GroupConfig(this, groupNames, pointConditions);
        return true;
    }

    private boolean setupDatabase() {
        FileConfiguration cf = getConfig();
        String storageType = cf.getString("player-data.storage");
        long saveInterval = cf.getLong("player-data.save-interval");
        switch (storageType.toLowerCase()) {
            case "mysql":
                String hostName = cf.getString("player-data.MySQL.hostname");
                int port = cf.getInt("player-data.MySQL.port");
                String databaseName = cf.getString("player-data.MySQL.database");
                String tableNameMySQL = cf.getString("player-data.MySQL.tablename");
                String parameters = cf.getString("player-data.MySQL.parameters");
                String userName = cf.getString("player-data.MySQL.username");
                String password = cf.getString("player-data.MySQL.password");
                try {
                    databaseManager = new DatabaseManager(
                        new Mysql(hostName, port, databaseName, parameters, tableNameMySQL,
                            userName, password));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
                break;
            case "sqlite":
            default:
                String tableName = cf.getString("player-data.SQLite.tablename");
                File file = new File(cf.getString("player-data.SQLite.file"));
                try {
                    databaseManager = new DatabaseManager(new Sqlite(tableName, file));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
                break;
        }
        playerDataManager = new PlayerDataManager(this);
        Bukkit.getScheduler()
            .runTaskTimer(this, playerDataManager::saveAllData, saveInterval * 20,
                saveInterval * 20);
        return true;
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        playerDataManager.close();
    }

    public Permission getPermission() {
        return perms;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Message getMessage() {
        return message;
    }

    public GroupConfig getGroupConfig() {
        return groupConfig;
    }
}
