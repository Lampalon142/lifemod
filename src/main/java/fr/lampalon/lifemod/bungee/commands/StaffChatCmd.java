package fr.lampalon.lifemod.bungee.commands;

import fr.lampalon.lifemod.bungee.utils.MessageUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import fr.lampalon.lifemod.bungee.LifeMod;
import net.md_5.bungee.api.plugin.TabExecutor;

public class StaffChatCmd extends Command implements TabExecutor {
    private final LifeMod plugin;

    public StaffChatCmd(LifeMod plugin) {
        super("staffchat", "lifemod.staffchat", "sc");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(MessageUtil.formatMessage(plugin.getLangYml().getString("staffchat.usage", "&cUsage: /sc <message>"))));
            return;
        }
        String message = String.join(" ", args);
        String format = MessageUtil.formatMessage(plugin.getLangYml().getString("staffchat.format", "%prefix% &7%server% &8» &e%sender%&8: &f%message%")
                .replace("%sender%", sender.getName())
                .replace("%message%", message)
                .replace("%server%", (sender instanceof ProxiedPlayer) ?
                        ((ProxiedPlayer) sender).getServer().getInfo().getName() : "Console"));
        ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("lifemod.staffchat"))
                .forEach(p -> p.sendMessage(new TextComponent(format)));
        ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(format));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return ProxyServer.getInstance().getServers().keySet();
    }
}
