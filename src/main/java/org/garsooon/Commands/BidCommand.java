package org.garsooon.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.garsooon.AuctionPlugin;

public class BidCommand implements CommandExecutor {
    private final AuctionPlugin plugin;

    public BidCommand(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("auctioneer.player")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to bid on an auction.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /bid <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid bid amount.");
            return true;
        }

        if (!plugin.getAuctionManager().bid(player, amount)) {
            player.sendMessage(ChatColor.RED + "Bid failed. You may have bid too low or you're the seller.");
        }

        return true;
    }
}
