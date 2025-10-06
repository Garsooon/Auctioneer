package org.garsooon.Economy.methods;


import me.zavdav.zcore.ZCore;
import me.zavdav.zcore.economy.BankAccount;
import me.zavdav.zcore.economy.PersonalAccount;
import me.zavdav.zcore.player.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.garsooon.Economy.Method;


import java.math.BigDecimal;

public class ZCoreEco implements Method {

    private ZCore zcore;

    public ZCore getPlugin() {
        return zcore;
    }

    public String getName() {
        return "ZCore";
    }

    public String getVersion() {
        return zcore.getDescription().getVersion();
    }

    public int fractionalDigits() {
        return 2;
    }

    public String format(double amount) {
        return ZCore.formatCurrency(BigDecimal.valueOf(amount));
    }

    public boolean hasBanks() {
        return true;
    }

    public boolean hasBank(String bank, World world) {
        return ZCore.getBank(bank) != null;
    }

    public boolean hasAccount(String name, World world) {
        return ZCore.getOfflinePlayer(name) != null;
    }

    public boolean hasBankAccount(String bank, String name, World world) {
        BankAccount account = ZCore.getBank(bank);
        return account != null && account.getOwner().getName().equalsIgnoreCase(name);
    }

    public MethodAccount getAccount(String name, World world) {
        OfflinePlayer player = ZCore.getOfflinePlayer(name);
        return player != null ? new ZCorePersonalAccount(player.getAccount()) : null;
    }

    public MethodBankAccount getBankAccount(String bank, String name, World world) {
        BankAccount account = ZCore.getBank(bank);
        return account != null && account.getOwner().getName().equalsIgnoreCase(name) ? new ZCoreBankAccount(account) : null;
    }

    public boolean isCompatible(Plugin plugin) {
        return plugin instanceof ZCore;
    }

    public void setPlugin(Plugin plugin) {
        if (!isCompatible(plugin)) return;
        this.zcore = (ZCore) plugin;
    }

    private static class ZCorePersonalAccount implements MethodAccount {

        private final PersonalAccount account;

        ZCorePersonalAccount(PersonalAccount account) {
            this.account = account;
        }

        public double balance(World world) {
            return account.getBalance().doubleValue();
        }

        public boolean set(double amount, World world) {
            return account.set(BigDecimal.valueOf(amount));
        }

        public boolean add(double amount, World world) {
            account.add(BigDecimal.valueOf(amount));
            return true;
        }

        public boolean subtract(double amount, World world) {
            return account.subtract(BigDecimal.valueOf(amount));
        }

        public boolean multiply(double amount, World world) {
            return account.multiply(BigDecimal.valueOf(amount));
        }

        public boolean divide(double amount, World world) {
            return account.divide(BigDecimal.valueOf(amount));
        }

        public boolean hasEnough(double amount, World world) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) >= 0;
        }

        public boolean hasOver(double amount, World world) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) > 0;
        }

        public boolean hasUnder(double amount, World world) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0;
        }

        public boolean isNegative(World world) {
            return account.getBalance().compareTo(BigDecimal.ZERO) < 0;
        }

        public boolean remove() {
            return false;
        }

        @Override
        public String toString() {
            return account.toString();
        }

    }

    private static class ZCoreBankAccount implements MethodBankAccount {

        private final BankAccount account;

        ZCoreBankAccount(BankAccount account) {
            this.account = account;
        }

        public double balance() {
            return account.getBalance().doubleValue();
        }

        public String getBankName() {
            return account.getName();
        }

        public int getBankId() {
            return 0;
        }

        public boolean set(double amount) {
            return account.set(BigDecimal.valueOf(amount));
        }

        public boolean add(double amount) {
            account.add(BigDecimal.valueOf(amount));
            return true;
        }

        public boolean subtract(double amount) {
            return account.subtract(BigDecimal.valueOf(amount));
        }

        public boolean multiply(double amount) {
            return account.multiply(BigDecimal.valueOf(amount));
        }

        public boolean divide(double amount) {
            return account.divide(BigDecimal.valueOf(amount));
        }

        public boolean hasEnough(double amount) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) >= 0;
        }

        public boolean hasOver(double amount) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) > 0;
        }

        public boolean hasUnder(double amount) {
            return account.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0;
        }

        public boolean isNegative() {
            return account.getBalance().compareTo(BigDecimal.ZERO) < 0;
        }

        public boolean remove() {
            return false;
        }

        @Override
        public String toString() {
            return account.toString();
        }

    }

}