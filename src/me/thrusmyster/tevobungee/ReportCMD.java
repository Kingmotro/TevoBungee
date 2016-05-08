package me.thrusmyster.tevobungee;

import java.util.HashMap;

import me.thrusmyster.tevobungee.Chat.ChatManager;
import me.thrusmyster.tevobungee.Util.CC;
import me.thrusmyster.tevobungee.Util.TimeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReportCMD extends Command{

	public ReportCMD() {
		super("report");
	}
	
	private HashMap<String, Long> cooldown = new HashMap<String, Long>();
	private String prefix = "§8[§2§lT§4§lN§7>§9Report§8] ";
	private TevoBungee main = TevoBungee.getInstance();
	
	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if (cooldown.containsKey(sender.getName()))
		{
			if (!TimeUtils.hasElapsed(cooldown.get(sender.getName()), 30000))
			{
				sender.sendMessage(TextComponent.fromLegacyText(prefix + "§cYou can only use this command every §a30 seconds§c!"));
				return;
			}
		}
		if (args.length > 1)
		{
			String player = args[0];
			if (main.getProxyServer().getPlayer(player) == null)
			{
				sender.sendMessage(TextComponent.fromLegacyText(prefix + "§cSpecified player not online!"));
				return;
			}
			StringBuilder reason = new StringBuilder();
			for (int index = 1; index < args.length; index++)
			{
				reason.append(args[index] + " ");
			}
			cooldown.put(sender.getName(), System.currentTimeMillis());
			ChatManager.sendStaffNotification(CC.tnInfo + "New report: from " + CC.tnPlayer + sender.getName() + CC.tnInfo + " targeting " + CC.tnPlayer + player + CC.tnInfo + " Reason: " + CC.cWhite + reason.toString());
			sender.sendMessage(TextComponent.fromLegacyText(prefix + CC.tnInfo + "A report has been sent!"));
		}
		else
		{
			sender.sendMessage(TextComponent.fromLegacyText(prefix + "§cNot enough arguments! §7/report <playername> <reason>"));
		}
	}
}
