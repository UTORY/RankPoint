package net.teamuni.rankpoint;

import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

public final class Message {

    private final FileConfiguration msgConf;

    Message(FileConfiguration msgConf) {
        this.msgConf = msgConf;
    }

    public String getMsg(String path, Object... objects) {
        if (msgConf.isSet(path)) {
            return String.format(msgConf.getString(path).replace('&', '§'), objects);
        } else {
            throw new YAMLException("message.yml : " + path + "를 불러올 수 없습니다.");
        }
    }
}
