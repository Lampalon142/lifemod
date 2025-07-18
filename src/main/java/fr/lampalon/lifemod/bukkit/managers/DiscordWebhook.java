package fr.lampalon.lifemod.bukkit.managers;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscordWebhook {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) { this.content = content; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTts(boolean tts) { this.tts = tts; }
    public void addEmbed(EmbedObject embed) { this.embeds.add(embed); }

    public void execute() throws Exception {
        if (content == null && embeds.isEmpty())
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");

        String json = buildJson();

        URL urlObj = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-Optimized");
        connection.setDoOutput(true);
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(json.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        }

        connection.getInputStream().close();
        connection.disconnect();
    }

    private String buildJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean hasContent = content != null;
        boolean hasUsername = username != null;
        boolean hasAvatar = avatarUrl != null;
        boolean hasEmbeds = !embeds.isEmpty();

        if (hasContent) appendJsonField(sb, "content", content, false);
        if (hasUsername) appendJsonField(sb, "username", username, hasContent);
        if (hasAvatar) appendJsonField(sb, "avatar_url", avatarUrl, hasContent || hasUsername);

        if (hasContent || hasUsername || hasAvatar) sb.append(",");
        sb.append("\"tts\":").append(tts);

        if (hasEmbeds) {
            sb.append(",\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                sb.append(embeds.get(i).toJson());
                if (i < embeds.size() - 1) sb.append(",");
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private void appendJsonField(StringBuilder sb, String key, String value, boolean comma) {
        if (comma) sb.append(",");
        sb.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class EmbedObject {
        private String title, description, url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        public Color getColor() { return color; }
        public Footer getFooter() { return footer; }
        public Thumbnail getThumbnail() { return thumbnail; }
        public Image getImage() { return image; }
        public Author getAuthor() { return author; }
        public List<Field> getFields() { return fields; }

        public EmbedObject setTitle(String title) { this.title = title; return this; }
        public EmbedObject setDescription(String description) { this.description = description; return this; }
        public EmbedObject setUrl(String url) { this.url = url; return this; }
        public EmbedObject setColor(Color color) { this.color = color; return this; }
        public EmbedObject setFooter(String text, String icon) { this.footer = new Footer(text, icon); return this; }
        public EmbedObject setThumbnail(String url) { this.thumbnail = new Thumbnail(url); return this; }
        public EmbedObject setImage(String url) { this.image = new Image(url); return this; }
        public EmbedObject setAuthor(String name, String url, String icon) { this.author = new Author(name, url, icon); return this; }
        public EmbedObject addField(String name, String value, boolean inline) { this.fields.add(new Field(name, value, inline)); return this; }

        private String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean comma = false;
            if (title != null) { sb.append("\"title\":\"").append(escapeJson(title)).append("\""); comma = true; }
            if (description != null) { if (comma) sb.append(","); sb.append("\"description\":\"").append(escapeJson(description)).append("\""); comma = true; }
            if (url != null) { if (comma) sb.append(","); sb.append("\"url\":\"").append(escapeJson(url)).append("\""); comma = true; }
            if (color != null) {
                if (comma) sb.append(",");
                int rgb = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
                sb.append("\"color\":").append(rgb);
                comma = true;
            }
            if (footer != null) { if (comma) sb.append(","); sb.append("\"footer\":").append(footer.toJson()); comma = true; }
            if (image != null) { if (comma) sb.append(","); sb.append("\"image\":").append(image.toJson()); comma = true; }
            if (thumbnail != null) { if (comma) sb.append(","); sb.append("\"thumbnail\":").append(thumbnail.toJson()); comma = true; }
            if (author != null) { if (comma) sb.append(","); sb.append("\"author\":").append(author.toJson()); comma = true; }
            if (!fields.isEmpty()) {
                if (comma) sb.append(",");
                sb.append("\"fields\":[");
                for (int i = 0; i < fields.size(); i++) {
                    sb.append(fields.get(i).toJson());
                    if (i < fields.size() - 1) sb.append(",");
                }
                sb.append("]");
            }
            sb.append("}");
            return sb.toString();
        }

        private String escapeJson(String text) {
            return text.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        private static class Footer {
            private final String text, iconUrl;
            private Footer(String text, String iconUrl) { this.text = text; this.iconUrl = iconUrl; }
            private String toJson() {
                StringBuilder sb = new StringBuilder("{\"text\":\"").append(escapeJson(text)).append("\"");
                if (iconUrl != null) sb.append(",\"icon_url\":\"").append(escapeJson(iconUrl)).append("\"");
                sb.append("}");
                return sb.toString();
            }
            private String escapeJson(String text) { return text.replace("\\", "\\\\").replace("\"", "\\\""); }
        }
        private static class Thumbnail {
            private final String url;
            private Thumbnail(String url) { this.url = url; }
            private String toJson() { return "{\"url\":\"" + escapeJson(url) + "\"}"; }
            private String escapeJson(String text) { return text.replace("\\", "\\\\").replace("\"", "\\\""); }
        }
        private static class Image {
            private final String url;
            private Image(String url) { this.url = url; }
            private String toJson() { return "{\"url\":\"" + escapeJson(url) + "\"}"; }
            private String escapeJson(String text) { return text.replace("\\", "\\\\").replace("\"", "\\\""); }
        }
        private static class Author {
            private final String name, url, iconUrl;
            private Author(String name, String url, String iconUrl) { this.name = name; this.url = url; this.iconUrl = iconUrl; }
            private String toJson() {
                StringBuilder sb = new StringBuilder("{\"name\":\"").append(escapeJson(name)).append("\"");
                if (url != null) sb.append(",\"url\":\"").append(escapeJson(url)).append("\"");
                if (iconUrl != null) sb.append(",\"icon_url\":\"").append(escapeJson(iconUrl)).append("\"");
                sb.append("}");
                return sb.toString();
            }
            private String escapeJson(String text) { return text.replace("\\", "\\\\").replace("\"", "\\\""); }
        }
        private static class Field {
            private final String name, value;
            private final boolean inline;
            private Field(String name, String value, boolean inline) { this.name = name; this.value = value; this.inline = inline; }
            private String toJson() {
                return "{\"name\":\"" + escapeJson(name) + "\",\"value\":\"" + escapeJson(value) + "\",\"inline\":" + inline + "}";
            }
            private String escapeJson(String text) { return text.replace("\\", "\\\\").replace("\"", "\\\""); }
        }
    }
}