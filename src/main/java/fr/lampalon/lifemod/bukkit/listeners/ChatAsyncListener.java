package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.NoteInputManager;
import fr.lampalon.lifemod.bukkit.models.StaffNote;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatAsyncListener implements Listener {
    private final LifeMod plugin;

    public ChatAsyncListener(LifeMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        NoteInputManager noteInputManager = plugin.getNoteInputManager();

        if (!noteInputManager.isPending(player)) return;

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            String message = event.getMessage();
            FileConfiguration lang = plugin.getLangConfig();

            NoteInputManager.NoteContext context = noteInputManager.getContext(player);
            noteInputManager.remove(player);

            if (message.equalsIgnoreCase("cancel")) {
                String cancelMsg = context.isEditMode()
                        ? MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.edit.cancelled", "&eEdit cancelled."))
                        : MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.chat.cancelled", "&eNote creation cancelled."));
                player.sendMessage(cancelMsg);
                plugin.getGuiManager().openStaffNotesMenu(player, context.getReport());
                return;
            }

            if (message.trim().isEmpty()) {
                player.sendMessage(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.errors.empty", "&cYour note cannot be empty.")));
                plugin.getGuiManager().openStaffNotesMenu(player, context.getReport());
                return;
            }

            if (context.isEditMode()) {
                StaffNote note = context.getNote();
                note.setContent(message);
                note.setUpdatedAt(System.currentTimeMillis());
                plugin.getDatabaseManager().getDatabaseProvider().updateStaffNote(note);
                player.sendMessage(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.edit.saved", "&aNote updated!")));
            } else {
                StaffNote newNote = new StaffNote(player.getUniqueId(), message);
                context.getReport().addStaffNote(newNote);
                plugin.getDatabaseManager().getDatabaseProvider().addStaffNote(context.getReport().getUuid(), newNote);
                player.sendMessage(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.chat.saved", "&aNote added!")));
            }

            plugin.getGuiManager().openStaffNotesMenu(player, context.getReport());
        });
    }
}