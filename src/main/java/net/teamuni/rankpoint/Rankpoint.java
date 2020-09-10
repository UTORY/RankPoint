package net.teamuni.rankpoint;

import java.util.LinkedHashMap;
import java.util.Objects;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Rankpoint extends JavaPlugin {

    private Permission perms;
    private LinkedHashMap<String, Integer> groupMap = new LinkedHashMap<>();

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
        ConfigurationSection groups = getConfig().getConfigurationSection("groups");
        if (groups == null) {
            getLogger().severe("config의 groups 설정을 불러오는데 실패했습니다.");
            return false;
        }
        groups.getKeys(false).stream().sorted().map(groups::getConfigurationSection)
            .filter(Objects::nonNull)
            .forEach(section -> groupMap.put(section.getString("group"), section.getInt("point")));
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Permission getPermission() {
        return perms;
    }
}
