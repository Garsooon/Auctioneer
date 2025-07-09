package org.garsooon.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.garsooon.AuctionPlugin;

public class AuctionCommand implements CommandExecutor {
    private final AuctionPlugin plugin;

    public AuctionCommand(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (!player.hasPermission("auctioneer.player")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to start an auction.");
            return true;
        }

        if (plugin.getAuctionManager().isAuctionRunning()) {
            player.sendMessage(ChatColor.RED + "An auction is already running.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.sendMessage(ChatColor.RED + "Usage: /auction <price> [MinBidIncrement]");
            player.sendMessage(ChatColor.RED + "Example increments: 5$ or 10%");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid price.");
            return true;
        }

        if (price <= 0) {
            player.sendMessage(ChatColor.RED + "Price must be greater than zero.");
            return true;
        }

        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item to auction.");
            return true;
        }

        String incrementArg = "";
        if (args.length == 2) {
            incrementArg = args[1];
        }

        // clone and remove from hand
        org.bukkit.inventory.ItemStack item = player.getItemInHand().clone();
        player.setItemInHand(null);

        if (!plugin.getAuctionManager().startAuction(player, item, price, incrementArg)) {
            player.getInventory().addItem(item); // return item on failure
            player.sendMessage(ChatColor.RED + "Auction failed. Item may be blacklisted or on cooldown.");
        }

        return true;
    }
}
