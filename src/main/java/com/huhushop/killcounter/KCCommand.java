package com.huhushop.killcounter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KCCommand implements CommandExecutor {

    private final KillCounter plugin;

    public KCCommand(KillCounter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /KC <True/False>");
            return false;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("true")) {
            plugin.setPlayerDisabled(player.getUniqueId(), false);
            player.sendMessage(ChatColor.GREEN + "KillCounter has been enabled.");
        } else if (arg.equals("false")) {
            plugin.setPlayerDisabled(player.getUniqueId(), true);
            player.sendMessage(ChatColor.RED + "KillCounter has been disabled.");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid argument. Use 'True' or 'False'.");
        }

        return true;
    }
}