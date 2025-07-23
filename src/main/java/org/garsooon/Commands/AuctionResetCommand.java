package org.garsooon.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.garsooon.AuctionPlugin;
import org.garsooon.AuctionManager;

import java.util.HashSet;
import java.util.Set;

public class AuctionResetCommand implements CommandExecutor {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final AuctionPlugin plugin;
    private final AuctionManager auctionManager;
    private final Set<String> confirmationSet = new HashSet<>();

    public AuctionResetCommand(AuctionPlugin plugin) {
        this.plugin = plugin;
        this.auctionManager = plugin.getAuctionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("auctioneer.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        String name = player.getName();

        if (!confirmationSet.contains(name)) {
            confirmationSet.add(name);
            player.sendMessage(ChatColor.RED + "This will reset the current auction and NOT give items or");
            player.sendMessage(ChatColor.RED + "money back! Run the command again to confirm.");
            return true;
        }

        confirmationSet.remove(name);
        auctionManager.forceEnd();
        player.sendMessage(ChatColor.GREEN + "Auction forcibly reset.");
        return true;
    }
}