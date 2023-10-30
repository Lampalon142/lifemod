package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
    
public class PlayerManager {
    Messages messages = (LifeMod.getInstance()).messages;
    Options options = (LifeMod.getInstance()).options;
    private Player player;
    private ItemStack[] items = new ItemStack[40];
    private boolean vanished;
    
    public PlayerManager(Player player) {
        this.player = player;
        this.vanished = true;
    }
    
    public void init() {
        this.messages = new Messages();
        this.options = new Options();
        LifeMod.getInstance().getPlayers().put(this.player.getUniqueId(), this);
        LifeMod.getInstance().getModerators().add(this.player.getUniqueId());
        this.player.sendMessage(messages.prefixGeneral + messages.modenable);
        saveInventory();
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
        
        ItemBuilder invSee = (new ItemBuilder(Material.PAPER)).setName(messages.nameinvsee).setLore(new String[] {messages.descinvsee });
        ItemBuilder freeze = (new ItemBuilder(Material.PACKED_ICE)).setName(messages.namefreeze).setLore(new String[] {messages.descfreeze });
        ItemBuilder tpRandom = (new ItemBuilder(Material.ENDER_PEARL)).setName(messages.nametprandom).setLore(new String[] {messages.desctprandom });
        ItemBuilder vanish = (new ItemBuilder(Material.BLAZE_POWDER)).setName(messages.namevanish).setLore(new String[] {messages.descvanish });
        ItemBuilder kill = (new ItemBuilder(Material.BLAZE_ROD)).setName(messages.namekill).setLore(new String[] {messages.desckill});
        ItemBuilder kbTester = new ItemBuilder(Material.STICK).setName(messages.namekbtester).setLore(new String[] {messages.desckbtester}).addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);

        this.player.getInventory().setItem(0, invSee.toItemStack());
        this.player.getInventory().setItem(2, freeze.toItemStack());
        this.player.getInventory().setItem(4, tpRandom.toItemStack());
        this.player.getInventory().setItem(6, vanish.toItemStack());
        this.player.getInventory().setItem(8, kill.toItemStack());
        this.player.getInventory().setItem(7, kbTester.toItemStack());
    }
    
    public void destroy() {
        LifeMod.getInstance().getPlayers().remove(this.player.getUniqueId());
        LifeMod.getInstance().getModerators().remove(this.player.getUniqueId());
        this.player.getInventory().clear();
        this.player.sendMessage(messages.prefixGeneral + messages.moddisable);
        giveInventory();
        this.player.setAllowFlight(false);
        this.player.setFlying(false);
        setVanished(false);
    }
    
    public static PlayerManager getFromPlayer(Player player) {
        return (PlayerManager)LifeMod.getInstance().getPlayers().get(player.getUniqueId());
    }
    
    public static boolean isInModerationMod(Player player) {
        return LifeMod.getInstance().getModerators().contains(player.getUniqueId());
    }
    
    public ItemStack[] getItems() {
        return this.items;
    }
    
    public boolean isVanished() {
        return this.vanished;
    }
    
    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        if (vanished) {
        Bukkit.getOnlinePlayers().forEach(players -> players.hidePlayer(this.player));
        } else {
        Bukkit.getOnlinePlayers().forEach(players -> players.showPlayer(this.player));
        } 
    }
    
    public void saveInventory() {
        for (int slot = 0; slot < 36; slot++) {
        ItemStack item = this.player.getInventory().getItem(slot);
        if (item != null) {
            this.items[slot] = item;
        }
        } 
        
        this.items[36] = this.player.getInventory().getHelmet();
        this.items[37] = this.player.getInventory().getChestplate();
        this.items[38] = this.player.getInventory().getLeggings();
        this.items[39] = this.player.getInventory().getBoots();
        
        this.player.getInventory().clear();
        this.player.getInventory().setHelmet(null);
        this.player.getInventory().setChestplate(null);
        this.player.getInventory().setLeggings(null);
        this.player.getInventory().setBoots(null);
    }
    
    public void giveInventory() {
        this.player.getInventory().clear();
        
        for (int slot = 0; slot < 36; slot++) {
        ItemStack item = this.items[slot];
        if (item != null) {
            this.player.getInventory().setItem(slot, item);
        }
        } 
        
        this.player.getInventory().setHelmet(this.items[36]);
        this.player.getInventory().setChestplate(this.items[37]);
        this.player.getInventory().setLeggings(this.items[38]);
        this.player.getInventory().setBoots(this.items[39]);
    }
}