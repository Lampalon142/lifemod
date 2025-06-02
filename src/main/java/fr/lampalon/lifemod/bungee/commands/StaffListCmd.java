package fr.lampalon.lifemod.bungee.commands;

import fr.lampalon.lifemod.bungee.utils.MessageUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import fr.lampalon.lifemod.bungee.LifeMod;

import java.util.List;

public class StaffListCmd extends Command {
    private final LifeMod plugin;

    public StaffListCmd(LifeMod plugin) {
        super("stafflist", "lifemod.stafflist", "sl");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<ProxiedPlayer> staffs = ProxyServer.getInstance().getPlayers().stream()
                .filter(p -> p.hasPermission("lifemod.staff"))
                .toList();

        if (staffs.isEmpty()) {
            sender.sendMessage(new TextComponent(MessageUtil.formatMessage(plugin.getLangYml().getString("stafflist.no-staff-online", "&cNo staff members are currently online."))));
            return;
        }

        StringBuilder staffList = new StringBuilder();
        String header = MessageUtil.formatMessage(plugin.getLangYml().getString("stafflist.header", "\n&6&lOnline Staff &8(&7%count%&8)\n"));
        String lineFormat = MessageUtil.formatMessage(plugin.getLangYml().getString("stafflist.line", " &8- &e%player% &7on &b%server%"));

        staffList.append(header.replace("%count%", String.valueOf(staffs.size())));
        for (ProxiedPlayer staff : staffs) {
            String line = lineFormat
                    .replace("%player%", staff.getName())
                    .replace("%server%", staff.getServer().getInfo().getName());
            staffList.append("\n").append(line);
        }
        sender.sendMessage(new TextComponent(staffList.toString()));
    }
}
