package org.garsooon.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.garsooon.AuctionManager;

public class PlayerJoinListener implements Listener {
    private final AuctionManager auctionManager;
    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin, AuctionManager auctionManager) {
        this.plugin = plugin;
        this.auctionManager = auctionManager;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!auctionManager.isAuctionRunning()) return;

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                Player player = event.getPlayer();
                String itemName = auctionManager.getCurrentItemDisplayName(); // You'll define this method below
                int itemAmount = auctionManager.getItemAmount();
                String sellerName = auctionManager.getCurrentSellerName();
                double currentBid = auctionManager.getCurrentBid();

                player.sendMessage(ChatColor.GOLD + "An auction is currently running!");
                player.sendMessage(ChatColor.GREEN + sellerName + " is auctioning " + ChatColor.YELLOW + itemAmount +
                        "x " + itemName + ChatColor.GREEN + " starting at $" + String.format("%.2f", currentBid));
            }
        }, 1L); //Tick delay to stop showing above motd
    }
}
