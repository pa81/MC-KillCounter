package com.huhushop.killcounter;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillListener implements Listener {

    private final KillCounter plugin;
    private final Map<UUID, Integer> killStreak = new HashMap<>();
    private final Map<UUID, BukkitTask> resetTasks = new HashMap<>();

    public KillListener(KillCounter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // プレイヤーがデスしたら、そのプレイヤーのキルストリークをリセット
        if (killStreak.containsKey(victim.getUniqueId())) {
            resetStreak(victim.getUniqueId());
        }

        // キルしたのがプレイヤーでなければ処理を終了
        if (victim.getKiller() == null) {
            return;
        }

        Player killer = victim.getKiller();

        // キラーが機能を無効にしている場合は処理を終了
        if (plugin.isPlayerDisabled(killer.getUniqueId())) {
            return;
        }

        // キルストリークを更新
        int currentStreak = killStreak.getOrDefault(killer.getUniqueId(), 0) + 1;
        killStreak.put(killer.getUniqueId(), currentStreak);

        // タイトルを表示
        killer.sendTitle(ChatColor.RED + "" + currentStreak + "Kill", "", 5, 40, 10);

        // サウンドを再生
        playSoundForKill(killer, currentStreak);

        // 連続キルリセットタイマーを設定
        scheduleStreakReset(killer.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // プレイヤーがログアウトしたらキルストリークをリセット
        resetStreak(event.getPlayer().getUniqueId());
    }

    private void playSoundForKill(Player player, int killCount) {
        // 5キル以降は5キル目と同じ音を再生
        int soundIndex = Math.min(killCount, 5);

        switch (soundIndex) {
            case 1:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                break;
            case 2:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.4f);
                break;
            case 3:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.6f);
                break;
            case 4:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.8f);
                break;
            case 5:
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                break;
        }
    }

    private void scheduleStreakReset(UUID playerUuid) {
        // 既存のリセットタスクがあればキャンセル
        if (resetTasks.containsKey(playerUuid)) {
            resetTasks.get(playerUuid).cancel();
        }

        // 15秒後にキルストリークをリセットするタスクをスケジュール
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                killStreak.remove(playerUuid);
                resetTasks.remove(playerUuid);
                // メッセージを送りたい場合はここに記述
                // Player player = plugin.getServer().getPlayer(playerUuid);
                // if(player != null) player.sendMessage("Kill streak reset.");
            }
        }.runTaskLater(plugin, 15 * 20L); // 15秒 * 20tick/秒

        resetTasks.put(playerUuid, task);
    }

    private void resetStreak(UUID playerUuid) {
        killStreak.remove(playerUuid);
        if (resetTasks.containsKey(playerUuid)) {
            resetTasks.get(playerUuid).cancel();
            resetTasks.remove(playerUuid);
        }
    }
}