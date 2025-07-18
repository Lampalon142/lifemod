package fr.lampalon.lifemod.bukkit.models;

import java.util.UUID;

public class StaffNote {
    private final UUID noteId;
    private final UUID author;
    private final long createdAt;
    private long updatedAt;
    private String content;

    public StaffNote(UUID noteId, UUID author, long createdAt, long updatedAt, String content) {
        this.noteId = noteId;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.content = content;
    }

    public StaffNote(UUID author, String content) {
        this(UUID.randomUUID(), author, System.currentTimeMillis(), System.currentTimeMillis(), content);
    }

    public UUID getNoteId() { return noteId; }
    public UUID getAuthor() { return author; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isEdited() {
        return updatedAt != createdAt;
    }

    public String getFormattedCreatedAt() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(createdAt));
    }

    public String getFormattedUpdatedAt() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(updatedAt));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaffNote)) return false;
        StaffNote note = (StaffNote) o;
        return noteId.equals(note.noteId);
    }

    @Override
    public int hashCode() {
        return noteId.hashCode();
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
