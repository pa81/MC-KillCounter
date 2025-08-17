package com.huhushop.killcounter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class KillCounter extends JavaPlugin {

    private final Set<UUID> disabledPlayers = new HashSet<>();
    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {
        // 設定ファイルの初期化
        createCustomConfig();
        loadDisabledPlayers();

        // イベントリスナーとコマンドの登録
        getServer().getPluginManager().registerEvents(new KillListener(this), this);
        getCommand("KC").setExecutor(new KCCommand(this));

        getLogger().info("KillCounter has been enabled!");
    }

    @Override
    public void onDisable() {
        saveDisabledPlayers();
        getLogger().info("KillCounter has been disabled!");
    }

    // --- 設定管理 ---

    public boolean isPlayerDisabled(UUID playerUuid) {
        return disabledPlayers.contains(playerUuid);
    }

    public void setPlayerDisabled(UUID playerUuid, boolean isDisabled) {
        if (isDisabled) {
            disabledPlayers.add(playerUuid);
        } else {
            disabledPlayers.remove(playerUuid);
        }
        saveDisabledPlayers(); // 変更を即座に保存
    }

// KillCounter.java の中の createCustomConfig メソッド

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "disabled_players.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            try {
                // saveResourceの代わりに、ファイルが存在しない場合のみ新規作成する
                customConfigFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create " + customConfigFile.getName());
                e.printStackTrace();
            }
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    private void loadDisabledPlayers() {
        List<String> disabledUuids = customConfig.getStringList("disabled");
        disabledPlayers.clear();
        for (String uuidString : disabledUuids) {
            try {
                disabledPlayers.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Could not parse UUID: " + uuidString);
            }
        }
    }

    private void saveDisabledPlayers() {
        List<String> disabledUuids = disabledPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        customConfig.set("disabled", disabledUuids);
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            getLogger().severe("Could not save config to " + customConfigFile);
            e.printStackTrace();
        }
    }
}