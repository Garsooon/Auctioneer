package org.garsooon.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.garsooon.AuctionManager;

public class PlayerJoinListener implements Listener {
    private final AuctionManager auctionManager;

    public PlayerJoinListener(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!auctionManager.isAuctionRunning()) return;

        Player player = event.getPlayer();
        String itemName = auctionManager.getCurrentItemDisplayName(); // You'll define this method below
        String sellerName = auctionManager.getCurrentSellerName();
        double currentBid = auctionManager.getCurrentBid();

        player.sendMessage(ChatColor.GOLD + "An auction is currently running!");
        player.sendMessage(ChatColor.YELLOW + sellerName + " is auctioning " + itemName +
                " starting at $" + String.format("%.2f", currentBid));
    }
}
