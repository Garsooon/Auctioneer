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

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Player player = event.getPlayer();
            String itemName = auctionManager.getCurrentItemDisplayName();
            int itemAmount = auctionManager.getItemAmount();
            String sellerName = auctionManager.getCurrentSellerName();
            double currentBid = auctionManager.getCurrentBid();

            @SuppressWarnings("unused") String durabilityInfo = "";
            org.bukkit.inventory.ItemStack item = auctionManager.getCurrentItem();
            short dur = item.getDurability();
            short maxDur = item.getType().getMaxDurability();
            if (maxDur > 0) {
                int remaining = maxDur - dur;
                int pct = (int) ((remaining * 100.0) / maxDur);
                ChatColor durColor = ChatColor.GREEN;
                if (pct <= 25) {
                    durColor = ChatColor.RED;
                } else if (pct <= 50) {
                    durColor = ChatColor.GOLD;
                } else if (pct <= 75) {
                    durColor = ChatColor.YELLOW;
                }
                //noinspection UnusedAssignment
                durabilityInfo = durColor + " [" + remaining + "/" + maxDur + " durability]";
            }

            player.sendMessage(ChatColor.GOLD + "An auction is currently running!");
            player.sendMessage(ChatColor.GREEN + sellerName + " is auctioning " + ChatColor.YELLOW + itemAmount +
                    "x " + itemName + ChatColor.GREEN + " starting at $" + String.format("%.2f", currentBid));
        }, 1L); //Tick delay to stop showing above motd
    }
}
