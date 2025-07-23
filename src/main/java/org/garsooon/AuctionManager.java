package org.garsooon;

import org.garsooon.Economy.Method;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class AuctionManager {
    private final AuctionPlugin plugin;
    private final Method economy;
    private Player currentSeller;
    private ItemStack currentItem;
    private double startPrice;
    private Player highestBidder;
    private double highestBid;
    private int taskId;
    private int countdownTaskId = -1;
//    private final Method economy = Methods.getMethod();
    private long auctionEndTime;
    private final Map<UUID, Long> lastAuctionTime = new HashMap<>();
    private long auctionStartTime = 0;

    // These control minimum bid increment rules
    private double minBidIncrement = 1.0; // fixed dollar amount
    private double percentBidIncrement = 0.0; // percent increment

    public AuctionManager(AuctionPlugin plugin, Method economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    /**
     * Starts an auction.
     * @param seller The player starting the auction
     * @param item The item to auction
     * @param price The starting price
     * @param incrementArg The increment argument: "$5" for fixed, "10%" for percent, or null/empty for default
     * @return true if auction started successfully
     */
    public boolean startAuction(Player seller, ItemStack item, double price, String incrementArg) {
        if (currentItem != null) return false;

        int cooldownMinutes = 5;
        Object rawCooldown = plugin.getCustomConfig().get("cooldown-minutes");
        if (rawCooldown instanceof Integer) {
            cooldownMinutes = (Integer) rawCooldown;
        }

        long now = System.currentTimeMillis();
        long last = lastAuctionTime.getOrDefault(seller.getUniqueId(), 0L);
        long elapsedMinutes = (now - last) / (1000 * 60);

        if (elapsedMinutes < cooldownMinutes) {
            long wait = cooldownMinutes - elapsedMinutes;
            seller.sendMessage(ChatColor.RED + "You must wait " + wait + " more minute" + (wait == 1 ? "" : "s") + " before starting another auction.");
            return false;
        }

        List<String> blacklist = new ArrayList<>();
        Object rawList = plugin.getCustomConfig().get("blacklist");
        if (rawList instanceof List) {
            for (Object o : (List<?>) rawList) {
                if (o instanceof String) blacklist.add((String) o);
            }
        }

        if (blacklist.contains(item.getType().name())) {
            seller.sendMessage(ChatColor.RED + "This item is blacklisted from auctions.");
            return false;
        }

        int duration = 60;
        Object durationObj = plugin.getCustomConfig().get("duration");
        if (durationObj instanceof Integer) duration = (Integer) durationObj;

        auctionStartTime = System.currentTimeMillis();
        auctionEndTime = auctionStartTime + (duration * 1000L);

        parseIncrement(incrementArg);

        currentSeller = seller;
        currentItem = item;
        startPrice = roundDown2(price);

        Object maxStartPriceObj = plugin.getCustomConfig().get("max-start-price");
        double maxStartPrice = 500.0;
        if (maxStartPriceObj instanceof Number) {
            maxStartPrice = ((Number) maxStartPriceObj).doubleValue();
        }
        if (startPrice > maxStartPrice) {
            seller.sendMessage(ChatColor.RED + "The maximum auction start price is $" + String.format("%.2f", maxStartPrice));
            return false;
        }

        highestBid = startPrice;
        highestBidder = null;
        auctionEndTime = System.currentTimeMillis() + (duration * 1000L);

        lastAuctionTime.put(seller.getUniqueId(), System.currentTimeMillis());

        Bukkit.broadcastMessage(ChatColor.GREEN + seller.getName() + " is auctioning " + ChatColor.YELLOW + item.getAmount() + "x " + getItemDisplayName(item) + ChatColor.GREEN + " starting at $" + startPrice);

        if (percentBidIncrement > 0.0) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "Minimum bid increase is set to " + percentBidIncrement + "%");
        } else {
            Bukkit.broadcastMessage(ChatColor.GRAY + "Minimum bid increase is set to $" + minBidIncrement);
        }

        scheduleAuctionEnd();

        return true;
    }

    // Parses the increment argument into either fixed or percentage increment.
    // Tried to enforce whole numbers as rarely the bid amount would not update when decimals were used
    @SuppressWarnings("UnnecessaryLocalVariable")
    private void parseIncrement(String arg) {
        if (arg == null || arg.isEmpty()) {
            minBidIncrement = 1.0;
            percentBidIncrement = 0.0;
            return;
        }
        arg = arg.trim();

        try {
            if (arg.endsWith("%")) {
                // Parse as integer percentage
                String numPart = arg.substring(0, arg.length() - 1);
                int percent = Integer.parseInt(numPart);
                percentBidIncrement = percent;
                minBidIncrement = 0.0;
            } else if (arg.startsWith("$")) {
                // Parse as integer fixed amount
                String numPart = arg.substring(1);
                int fixed = Integer.parseInt(numPart);
                minBidIncrement = fixed;
                percentBidIncrement = 0.0;
            } else if (arg.endsWith("$")) {
                // Parse as integer fixed amount
                String numPart = arg.substring(0, arg.length() - 1);
                int fixed = Integer.parseInt(numPart);
                minBidIncrement = fixed;
                percentBidIncrement = 0.0;
            } else {
                // Parse as integer fixed amount
                int fixed = Integer.parseInt(arg);
                minBidIncrement = fixed;
                percentBidIncrement = 0.0;
            }
        } catch (NumberFormatException e) {
            // On error fallback to default
            minBidIncrement = 1.0;
            percentBidIncrement = 0.0;
        }
    }

    //Handles placing a bid on the current auction.
    public boolean bid(Player bidder, double amount) {
        if (currentItem == null || bidder == currentSeller) return false;

        amount = roundDown2(amount);

        if (highestBidder != null) {
            double requiredMin = highestBid + minBidIncrement;
            if (percentBidIncrement > 0.0) {
                requiredMin = highestBid + (highestBid * percentBidIncrement / 100.0);
            }
            requiredMin = roundDown2(requiredMin);

            if (amount < requiredMin) {
                bidder.sendMessage(ChatColor.RED + "You must bid at least $" + String.format("%.2f", requiredMin) + ".");
                return false;
            }
        } else {
            if (amount < startPrice) {
                bidder.sendMessage(ChatColor.RED + "You must bid at least the starting price of $" + String.format("%.2f", startPrice) + ".");
                return false;
            }
        }

        Method.MethodAccount bidderAccount = economy.getAccount(bidder.getName(), bidder.getWorld());
        if (bidderAccount == null || !bidderAccount.hasEnough(amount, bidder.getWorld())) {
            bidder.sendMessage(ChatColor.RED + "You don't have enough money to bid $" + amount);
            return false;
        }

        if (highestBidder != null) {
            Method.MethodAccount previous = economy.getAccount(highestBidder.getName(), highestBidder.getWorld());
            if (previous != null) {
                previous.add(highestBid, highestBidder.getWorld());
                highestBidder.sendMessage(ChatColor.YELLOW + "Your previous bid of $" + highestBid + " was refunded.");
            }
        }

        if (!bidderAccount.subtract(amount, bidder.getWorld())) {
            bidder.sendMessage(ChatColor.RED + "Failed to withdraw money.");
            return false;
        }

        highestBid = amount;
        highestBidder = bidder;
        Bukkit.broadcastMessage(ChatColor.AQUA + bidder.getName() + " bids $" + amount);

        int timeAddPerBid = 10;
        Object bidAddObj = plugin.getCustomConfig().get("time_add_per_bid");
        if (bidAddObj instanceof Integer) timeAddPerBid = (Integer) bidAddObj;

        int maxTime = 180;
        Object maxAuctionTimeObj = plugin.getCustomConfig().get("max_auction_time");
        if (maxAuctionTimeObj instanceof Integer) maxTime = (Integer) maxAuctionTimeObj;

        long now = System.currentTimeMillis();
        long remaining = auctionEndTime - now;
        long newRemaining = remaining + (timeAddPerBid * 1000L);
        long maxRemaining = maxTime * 1000L;

        if (newRemaining > maxRemaining) newRemaining = maxRemaining;

        auctionEndTime = now + newRemaining;
        Bukkit.getScheduler().cancelTask(taskId);
        if (countdownTaskId != -1) Bukkit.getScheduler().cancelTask(countdownTaskId);
        scheduleAuctionEnd();

        Bukkit.broadcastMessage(ChatColor.GRAY + "Auction time extended. " + (newRemaining / 1000) + " seconds remain.");

        return true;
    }

    private double roundDown2(double val) {
        return Math.floor(val * 100.0) / 100.0;
    }

    private void scheduleAuctionEnd() {
        long delay = (auctionEndTime - System.currentTimeMillis()) / 50L;
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::endAuction, delay);

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            private int lastAnnounced = -1;
            @Override
            public void run() {
                if (currentItem == null) {
                    Bukkit.getScheduler().cancelTask(countdownTaskId);
                    return;
                }
                long timeLeft = (auctionEndTime - System.currentTimeMillis()) / 1000L;
                if ((timeLeft == 10 || timeLeft == 5) && lastAnnounced != (int) timeLeft) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "Auction ends in " + timeLeft + " seconds!");
                    lastAnnounced = (int) timeLeft;
                }
                if (timeLeft <= 0) Bukkit.getScheduler().cancelTask(countdownTaskId);
            }
        }, 0L, 20L);
    }

    public void endAuction() {
        if (currentItem == null) return;

        if (highestBidder != null) {
            HashMap<Integer, ItemStack> leftover = highestBidder.getInventory().addItem(currentItem);
            if (!leftover.isEmpty()) {
                for (ItemStack stack : leftover.values()) {
                    highestBidder.getWorld().dropItemNaturally(highestBidder.getLocation(), stack);
                }
                highestBidder.sendMessage(ChatColor.YELLOW + "Your inventory was full! The auction item was dropped at your feet.");
            }

            Bukkit.broadcastMessage(ChatColor.GOLD + highestBidder.getName() + " won the auction for $" + highestBid);

            if (economy != null) {
                Method.MethodAccount sellerAccount = economy.getAccount(currentSeller.getName(), currentSeller.getWorld());
                if (sellerAccount != null) {
                    sellerAccount.add(highestBid, currentSeller.getWorld());
                    currentSeller.sendMessage(ChatColor.GREEN + "You received $" + highestBid + " from the auction.");
                }
            }
        } else {
            HashMap<Integer, ItemStack> leftover = currentSeller.getInventory().addItem(currentItem);
            if (!leftover.isEmpty()) {
                for (ItemStack stack : leftover.values()) {
                    currentSeller.getWorld().dropItemNaturally(currentSeller.getLocation(), stack);
                }
                currentSeller.sendMessage(ChatColor.YELLOW + "Your inventory was full! The item was dropped at your feet.");
            }

            Bukkit.broadcastMessage(ChatColor.RED + "Auction ended with no bids.");
        }

        currentItem = null;
        currentSeller = null;
        highestBid = 0;
        highestBidder = null;
    }

    //This won't give items back, because its use case is for when an auction is completed but stuck.
    public void forceEnd() {
        this.currentItem = null;
        this.currentSeller = null;
        this.highestBidder = null;
        this.highestBid = 0;
        this.auctionEndTime = 0;
        this.percentBidIncrement = 0.0;
        this.minBidIncrement = 1.0;

        Bukkit.broadcastMessage(ChatColor.RED + "The current auction has been forcibly reset by an admin.");
    }

    public void cleanupStuckAuction() {
        if (currentItem == null) return;

        long maxAuctionTime = 30; // fallback
        Object rawMax = plugin.getCustomConfig().get("max_auction_time");
        if (rawMax instanceof Integer) {
            maxAuctionTime = (Integer) rawMax;
        }

        long now = System.currentTimeMillis();
        long gracePeriodEnd = auctionEndTime + (maxAuctionTime * 1000L);

        if (now > gracePeriodEnd && auctionStartTime < (now - (maxAuctionTime * 1000L))) {
            Bukkit.broadcastMessage(ChatColor.RED + "Stuck auction forcibly reset due to timeout.");
            getLogger().warning("[Auctioneer] An auction was forcibly reset due to timeout.");
            forceEnd();
        }
    }

    public String getCurrentItemDisplayName() {
        return currentItem != null ? getItemDisplayName(currentItem) : "Unknown Item";
    }

    public int getItemAmount() {
        return currentItem != null ? currentItem.getAmount() : 0;
    }

    public String getCurrentSellerName() {
        return currentSeller != null ? currentSeller.getName() : "Unknown";
    }

    public double getCurrentBid() {
        return Math.floor(highestBid * 100) / 100.0;
    }

    public boolean isAuctionRunning() {
        return currentItem != null;
    }

    // Handler and case returns for items with internal data values
    private String getItemDisplayName(ItemStack item) {
        int id = item.getTypeId();
        int data = item.getDurability();

        switch (id) {
            case 337: return "Clay Ball";
            case 82: return "Clay Block";
            case 35: return getWoolName(data);
            case 351: return getDyeName(data);
            case 17: return getLogName(data);
            case 18: return getLeafName(data);
            case 6: return getSaplingName(data);
            case 44: return getSlabName(data);
            default: return item.getType().name().replace('_', ' ').toLowerCase();
        }
    }

    private String getWoolName(int data) {
        switch (data) {
            case 0: return "White Wool";
            case 1: return "Orange Wool";
            case 2: return "Magenta Wool";
            case 3: return "Light Blue Wool";
            case 4: return "Yellow Wool";
            case 5: return "Lime Wool";
            case 6: return "Pink Wool";
            case 7: return "Gray Wool";
            case 8: return "Light Gray Wool";
            case 9: return "Cyan Wool";
            case 10: return "Purple Wool";
            case 11: return "Blue Wool";
            case 12: return "Brown Wool";
            case 13: return "Green Wool";
            case 14: return "Red Wool";
            case 15: return "Black Wool";
            default: return "Wool";
        }
    }

    private String getDyeName(int data) {
        switch (data) {
            case 0: return "Ink Sack (Black Dye)";
            case 1: return "Red Dye"; case 2:
                return "Cactus Green (Green Dye)";
            case 3: return "Cocoa Beans (Brown Dye)";
            case 4: return "Lapis Lazuli (Blue Dye)";
            case 5: return "Purple Dye";
            case 6: return "Cyan Dye";
            case 7: return "Light Gray Dye";
            case 8: return "Gray Dye";
            case 9: return "Pink Dye";
            case 10: return "Lime Dye";
            case 11: return "Yellow Dye";
            case 12: return "Light Blue Dye";
            case 13: return "Magenta Dye";
            case 14: return "Orange Dye";
            case 15: return "Bone Meal (White Dye)";
            default: return "Unknown Dye";
        }
    }

    private String getLogName(int data) {
        switch (data) {
            case 0: return "Oak Log";
            case 1: return "Spruce Log";
            case 2: return "Birch Log";
            default: return "Unknown Log";
        }
    }

    private String getLeafName(int data) {
        switch (data) {
            case 0: return "Oak Leaves";
            case 1: return "Spruce Leaves";
            case 2: return "Birch Leaves";
            default: return "Unknown Leaves";
        }
    }

    private String getSaplingName(int data) {
        switch (data) {
            case 0: return "Oak Sapling";
            case 1: return "Spruce Sapling";
            case 2: return "Birch Sapling";
            default: return "Unknown Sapling";
        }
    }

    private String getSlabName(int data) {
        switch (data) {
            case 0: return "Stone Slab";
            case 1: return "Sandstone Slab";
            case 2: return "Wooden Slab";
            case 3: return "Cobblestone Slab";
            default: return "Unknown Slab";
        }
    }
}
