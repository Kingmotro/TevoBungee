package me.thrusmyster.tevobungee.Chat;

import me.thrusmyster.tevobungee.TevoBungee;
import me.thrusmyster.tevobungee.Networking.TevoNetworkMessageTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ChatManager {

	private static TevoBungee main = TevoBungee.getInstance();
	
	public static void sendNetworkBroadcast(String msg, Server server)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		
		out.writeUTF("NetworkBroadcast");
		out.writeUTF(msg);
	
		for (ServerInfo s : TevoBungee.getInstance().getProxyServer().getServers().values())
		{
			if ((!s.getName().equals(server.getInfo().getName())) && (s.getPlayers().size() > 0))
			{
				main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(s, out.toByteArray()));
			}
		}
		
	}
	
	public static void sendPrivateMessage(String sendername, String senderdispname, String subjectname, String msg, Server server)//Should we do cross server private msg? YES
	{
		if (TevoBungee.getInstance().getProxyServer().getPlayer(subjectname) != null)
		{
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("PrivateChat");
			out.writeUTF(sendername);
			out.writeUTF(senderdispname);
			out.writeUTF(subjectname);
			out.writeUTF(msg);
		
			for (ServerInfo s : TevoBungee.getInstance().getProxyServer().getServers().values())
			{
				if ((!s.getName().equals(server.getInfo().getName())) && (s.getPlayers().size() > 0))
				{
					main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(s, out.toByteArray()));
				}
			}
		}
		else
		{
			
			TevoBungee.getInstance().getProxyServer().getPlayer(sendername).sendMessage(
					new ComponentBuilder("[").color(ChatColor.DARK_GRAY)
					.append("T").color(ChatColor.DARK_GREEN).bold(true)
					.append("N").color(ChatColor.DARK_RED).bold(true)
					.append(">").color(ChatColor.DARK_GRAY).bold(false)
					.append("Chat").color(ChatColor.BLUE)
					.append("] ").color(ChatColor.DARK_GRAY)
					.append("The player is not online!").color(ChatColor.RED)
					.create());
		}
	}
	
	public static void sendStaffChat(String senderdispname, String msg, Server server)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		
		out.writeUTF("StaffChat");
		out.writeUTF(senderdispname);
		out.writeUTF(msg);
		
		for (ServerInfo s : TevoBungee.getInstance().getProxyServer().getServers().values())
		{
			if ((!s.getName().equals(server.getInfo().getName())) && (s.getPlayers().size() > 0))
			{
				main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(s, out.toByteArray()));
			}
		}
	}
	
	public static void sendStaffNotification(String msg)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("StaffNotification");
		out.writeUTF(msg);
		
		for (ServerInfo s : main.getProxyServer().getServers().values())
		{
			if (s.getPlayers().size() > 0)
			{
				main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(s, out.toByteArray()));
			}
		}
	}
	
	public static void sendGlobalChat(String senderdispname, String msg, Server server)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("GlobalChat");
		out.writeUTF(senderdispname);
		out.writeUTF(msg);
		
		for (ServerInfo s : TevoBungee.getInstance().getProxyServer().getServers().values())
		{
			if ((!s.getName().equals(server.getInfo().getName())) && (s.getPlayers().size() > 0))
			{
				main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(s, out.toByteArray()));
			}
		}
	}
	
}
