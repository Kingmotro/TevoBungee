package me.thrusmyster.tevobungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class TNCMD extends Command {

	public TNCMD() {
		super("tnreload");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			TevoBungee.getInstance().reloadConfig();
			TevoBungee.getInstance().getUtilLogger().info("Config Reloaded!");
		}
	}

}
