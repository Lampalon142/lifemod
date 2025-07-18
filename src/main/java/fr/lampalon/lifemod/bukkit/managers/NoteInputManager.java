package fr.lampalon.lifemod.bukkit.managers;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.StaffNote;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoteInputManager {
    private final LifeMod plugin;
    private final Map<UUID, NoteContext> pendingInputs = new HashMap<>();

    public NoteInputManager(LifeMod plugin) {
        this.plugin = plugin;
    }

    public void startNoteInput(Player player, Report report) {
        pendingInputs.put(player.getUniqueId(), new NoteContext(report, null));
    }

    public void startNoteEdit(Player player, Report report, StaffNote note) {
        pendingInputs.put(player.getUniqueId(), new NoteContext(report, note));
    }

    public boolean isPending(Player player) {
        return pendingInputs.containsKey(player.getUniqueId());
    }

    public void cancel(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }

    public NoteContext getContext(Player player) {
        return pendingInputs.get(player.getUniqueId());
    }

    public void remove(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }

    public void handleChatInput(Player player, String message) {
        NoteContext context = pendingInputs.remove(player.getUniqueId());
        if (context == null) return;

        FileConfiguration lang = plugin.getLangConfig();

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
            context.getNote().setContent(message);
            context.getNote().setUpdatedAt(System.currentTimeMillis());
            plugin.getDatabaseManager().getDatabaseProvider().updateStaffNote(context.getNote());
            player.sendMessage(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.edit.saved", "&aNote updated!")));
        } else {
            StaffNote newNote = new StaffNote(player.getUniqueId(), message);
            context.getReport().addStaffNote(newNote);
            plugin.getDatabaseManager().getDatabaseProvider().addStaffNote(context.getReport().getUuid(), newNote);
            player.sendMessage(MessageUtil.formatMessage(lang.getString("report.detail.notes.menu.chat.saved", "&aNote added!")));
        }
        plugin.getGuiManager().openStaffNotesMenu(player, context.getReport());
    }

    public static class NoteContext {
        private final Report report;
        private final StaffNote note;

        public NoteContext(Report report, StaffNote note) {
            this.report = report;
            this.note = note;
        }

        public boolean isEditMode() {
            return note != null;
        }

        public Report getReport() {
            return report;
        }

        public StaffNote getNote() {
            return note;
        }
    }
}
