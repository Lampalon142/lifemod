package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
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
    private Player player;
    private ItemStack[] items = new ItemStack[40];
    VanishedManager vanished =  new VanishedManager();
    private LifeMod plugin;
    
    public PlayerManager(Player player) {
        this.player = player;
    }
    
    public void init() {
        LifeMod.getInstance().getPlayers().put(this.player.getUniqueId(), this);
        LifeMod.getInstance().getModerators().add(this.player.getUniqueId());
        this.player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("mod.enable")));
        saveInventory();
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
        this.player.setInvulnerable(true);
        this.player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(1000000000,255));
        vanished.setVanished(true, this.player);

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.invSee")) {
            ItemBuilder invSee = (new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.invsee.material")))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.invsee.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.invsee.description"))});
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.kbtester.slot"), invSee.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.freeze")) {
            ItemBuilder freeze = (new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.freeze.material")))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.freeze.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.freeze.description"))});
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.freeze.slot"), freeze.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.tpRandom")) {
            ItemBuilder tpRandom = (new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.tprandom.material")))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.tprandom.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.tprandom.description"))});
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.tprandom.slot"), tpRandom.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.vanish")) {
            ItemBuilder vanish = (new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.vanish.material")))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.vanish.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.vanish.description"))});
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.vanish.slot"), vanish.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.killItem")) {
            ItemBuilder kill = (new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.kill.material")))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.kill.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.kill.description"))});
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.kill.slot"), kill.toItemStack());
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("items-enabled.kbTester")) {
            ItemBuilder kbTester = new ItemBuilder(Material.valueOf(LifeMod.getInstance().getLangConfig().getString("tools.kbtester.material"))).setName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.kbtester.name"))).setLore(new String[]{MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("tools.kbtester.description"))}).addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
            this.player.getInventory().setItem(LifeMod.getInstance().getLangConfig().getInt("tools.kbtester.slot"), kbTester.toItemStack());
        }
    }
    
    public void destroy() {
        LifeMod.getInstance().getPlayers().remove(this.player.getUniqueId());
        LifeMod.getInstance().getModerators().remove(this.player.getUniqueId());
        this.player.getInventory().clear();
        this.player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("mod.disable")));
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