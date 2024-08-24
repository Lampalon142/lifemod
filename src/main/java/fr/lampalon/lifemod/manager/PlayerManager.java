package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.utils.ItemBuilder;
import fr.lampalon.lifemod.utils.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerManager {
    Messages messages = (LifeMod.getInstance()).messages;
    Options options = (LifeMod.getInstance()).options;
    private Player player;
    private ItemStack[] items = new ItemStack[40];
    VanishedManager vanished =  new VanishedManager();
    private LifeMod plugin;
    
    public PlayerManager(Player player) {
        this.player = player;
    }
    
    public void init() {
        this.messages = new Messages();
        this.options = new Options();
        LifeMod.getInstance().getPlayers().put(this.player.getUniqueId(), this);
        LifeMod.getInstance().getModerators().add(this.player.getUniqueId());
        this.player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.modenable));
        saveInventory();
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
        this.player.setInvulnerable(true);
        this.player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(1000000000,255));
        vanished.setVanished(true, this.player);

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.invSee")) {
            ItemBuilder invSee = (new ItemBuilder(Material.PAPER)).setName(MessageUtil.parseColors(messages.nameinvsee)).setLore(new String[]{MessageUtil.parseColors(messages.descinvsee)});
            this.player.getInventory().setItem(0, invSee.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.freeze")) {
            ItemBuilder freeze = (new ItemBuilder(Material.PACKED_ICE)).setName(MessageUtil.parseColors(messages.namefreeze)).setLore(new String[]{MessageUtil.parseColors(messages.descfreeze)});
            this.player.getInventory().setItem(2, freeze.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.tpRandom")) {
            ItemBuilder tpRandom = (new ItemBuilder(Material.ENDER_PEARL)).setName(MessageUtil.parseColors(messages.nametprandom)).setLore(new String[]{MessageUtil.parseColors(messages.desctprandom)});
            this.player.getInventory().setItem(4, tpRandom.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.vanish")) {
            ItemBuilder vanish = (new ItemBuilder(Material.BLAZE_POWDER)).setName(MessageUtil.parseColors(messages.namevanish)).setLore(new String[]{MessageUtil.parseColors(messages.descvanish)});
            this.player.getInventory().setItem(6, vanish.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.killItem")) {
            ItemBuilder kill = (new ItemBuilder(Material.BLAZE_ROD)).setName(MessageUtil.parseColors(messages.namekill)).setLore(new String[]{MessageUtil.parseColors(messages.desckill)});
            this.player.getInventory().setItem(8, kill.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.kbTester")) {
            ItemBuilder kbTester = new ItemBuilder(Material.STICK).setName(MessageUtil.parseColors(messages.namekbtester)).setLore(new String[]{MessageUtil.parseColors(messages.desckbtester)}).addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
            this.player.getInventory().setItem(7, kbTester.toItemStack());
        }
    }
    
    public void destroy() {
        LifeMod.getInstance().getPlayers().remove(this.player.getUniqueId());
        LifeMod.getInstance().getModerators().remove(this.player.getUniqueId());
        this.player.getInventory().clear();
        this.player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.moddisable));
        giveInventory();
        this.player.setAllowFlight(false);
        this.player.setFlying(false);
        this.player.setInvulnerable(false);
        this.player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        vanished.setVanished(false, player);
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