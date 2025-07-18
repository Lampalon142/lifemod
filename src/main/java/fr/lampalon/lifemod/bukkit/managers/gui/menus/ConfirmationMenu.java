package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ConfirmationMenu {
    private final LifeMod plugin;
    private final Player player;
    private final String actionType;
    private final UUID noteId;
    private final Report report;

    public ConfirmationMenu(LifeMod plugin, Player player, String actionType, UUID noteId, Report report) {
        this.plugin = plugin;
        this.player = player;
        this.actionType = actionType;
        this.noteId = noteId;
        this.report = report;
    }

    public void open() {
        FileConfiguration lang = plugin.getLangConfig();
        String title = MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.remove.confirm_title", "&cConfirm Delete"));
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack yes = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.remove.yes", "&aYes, delete")));
        yesMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "action_type"), PersistentDataType.STRING, actionType);
        yesMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "note_id"), PersistentDataType.STRING, noteId.toString());
        yesMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "report_uuid"),
                PersistentDataType.STRING,
                report.getUuid().toString()
        );
        yes.setItemMeta(yesMeta);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.remove.no", "&cNo, cancel")));
        noMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "action_type"), PersistentDataType.STRING, "cancel_delete_note");
        noMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "report_uuid"),
                PersistentDataType.STRING,
                report.getUuid().toString()
        );
        no.setItemMeta(noMeta);

        inv.setItem(11, yes);
        inv.setItem(15, no);

        player.openInventory(inv);
    }
}
