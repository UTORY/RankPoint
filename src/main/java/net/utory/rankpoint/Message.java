package net.utory.rankpoint;

import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

public final class Message {

    private final FileConfiguration msgConf;

    Message(FileConfiguration msgConf) {
        this.msgConf = msgConf;
    }

    public String getMsg(String path, String name, int point) {
        if (msgConf.isSet(path)) {
            return msgConf.getString(path).replace('&', '§').replace("<player>", name)
                .replace("<point>", point + "");
        } else {
            throw new YAMLException("message.yml : " + path + "를 불러올 수 없습니다.");
        }
    }

    public String getMsg(String path, String name) {
        return getMsg(path, name, 0);
    }

    public String getMsg(String path, int point) {
        return getMsg(path, "", point);
    }

    public String getMsg(String path) {
        return getMsg(path, "");
    }
}
