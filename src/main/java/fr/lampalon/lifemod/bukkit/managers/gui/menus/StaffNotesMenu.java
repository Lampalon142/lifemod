package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.StaffNote;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StaffNotesMenu {
    private final LifeMod plugin;
    private final Player player;
    private final Report report;

    public StaffNotesMenu(LifeMod plugin, Player player, Report report) {
        this.plugin = plugin;
        this.player = player;
        this.report = report;
    }

    public void open() {
        FileConfiguration lang = plugin.getLangConfig();
        String title = MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.title", "&bStaff Notes"));
        int size = 27;
        Inventory inv = Bukkit.createInventory(null, size, title);

        List<StaffNote> notes = report.getStaffNotes();

        List<Integer> noteSlots = lang.getIntegerList("report.detail.notes.menu.notes_slots");
        if (noteSlots == null || noteSlots.isEmpty()) {
            noteSlots = new ArrayList<>();
            for (int i = 0; i < size - 1; i++) noteSlots.add(i);
        }
        int addNoteSlot = lang.getInt("report.detail.notes.menu.add_note_slot", size - 1);

        if (notes.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.empty", "&7No staff notes for this report.")));
            empty.setItemMeta(meta);
            inv.setItem(size / 2, empty);
        } else {
            for (int i = 0; i < notes.size() && i < noteSlots.size(); i++) {
                StaffNote note = notes.get(i);
                inv.setItem(noteSlots.get(i), buildNoteItem(note));
            }
        }

        if (!noteSlots.contains(addNoteSlot)) {
            inv.setItem(addNoteSlot, buildAddNoteItem());
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (!noteSlots.contains(i)) {
                    inv.setItem(i, buildAddNoteItem());
                    break;
                }
            }
        }

        player.openInventory(inv);
    }

    private ItemStack buildNoteItem(StaffNote note) {
        FileConfiguration lang = plugin.getLangConfig();
        String name = MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.item.name", "&eNote by %author%")
                .replace("%author%", getPlayerName(note.getAuthor())));
        List<String> lore = new ArrayList<>();
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(note.getCreatedAt()));
        String edited = note.isEdited() ? "&7(edited)" : "";
        for (String line : lang.getStringList("report.detail.notes.menu.item.lore")) {
            lore.add(MessageUtil.formatMessage(line
                    .replace("%date%", date)
                    .replace("%content%", note.getContent())
                    .replace("%note_id%", note.getNoteId().toString())
                    .replace("%edited%", edited))
            );
        }
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "note_id"),
                PersistentDataType.STRING,
                note.getNoteId().toString()
        );

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "action_type"),
                PersistentDataType.STRING,
                "staff_note"
        );

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "report_uuid"),
                PersistentDataType.STRING,
                report.getUuid().toString()
        );
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildAddNoteItem() {
        FileConfiguration lang = plugin.getLangConfig();
        String name = MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.add.name", "&aAdd Note"));

        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList("report.detail.notes.menu.add.lore")) {
            lore.add(MessageUtil.formatMessage(line));
        }
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "action_type"),
                PersistentDataType.STRING,
                "add_note"
        );
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "report_uuid"),
                PersistentDataType.STRING,
                report.getUuid().toString()
        );
        item.setItemMeta(meta);
        return item;
    }

    private String getPlayerName(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return (p != null && p.getName() != null) ? p.getName() : "Unknown";
    }
}
