package me.thrusmyster.tevobungee;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.thrusmyster.tevobungee.Chat.ChatManager;
import me.thrusmyster.tevobungee.Punish.Punish;
import me.thrusmyster.tevobungee.Punish.Objects.Ban;
import me.thrusmyster.tevobungee.Punish.Objects.Mute;
import me.thrusmyster.tevobungee.SQL.Logins;
import me.thrusmyster.tevobungee.Util.UUIDFetcher;
import me.thrusmyster.tevobungee.Voting.VoteManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Listeners implements Listener {
	
	private TevoBungee main = TevoBungee.getInstance();
	private List<String> blockedCMDs = Arrays.asList("/tell", "/msg", "/r", "/reply");
	
	@EventHandler
	public void onPing(ProxyPingEvent e)
	{
		if (e.getResponse() == null)
		{
			return;
		}
		ServerPing response = e.getResponse();
		response.setDescription(ChatColor.translateAlternateColorCodes('&', TevoBungee.getInstance().getConfig().getString("motd")).replaceAll("%newline%", "\n"));
		ServerPing.Players players = response.getPlayers();
		players = new ServerPing.Players(players.getOnline() + 1, players.getOnline(), players.getSample());
		response.setPlayers(players);
		e.setResponse(response);
	}
	
	@EventHandler
	public void onJoin(PostLoginEvent e)
	{
		Logins.incrementLogins(e.getPlayer().getName());
	}
	
	@EventHandler
	public void onLogout(PlayerDisconnectEvent e)
	{
		UUIDFetcher.removefromCache(e.getPlayer().getName());
		VoteManager.removePlayer(e.getPlayer().getName());
	}
	
	@EventHandler
	public void onServerCon(ServerConnectedEvent e)
	{
		VoteManager.addPlayer(e.getPlayer().getName());
		e.getPlayer().setTabHeader(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("header")).replaceAll("%newline%", "\\n")), TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("footer")).replaceAll("%newline%", "\\n")));
	}
	
	@EventHandler
	public void onChat(ChatEvent e)
	{
		Mute mute = null;
		ProxiedPlayer sender = (ProxiedPlayer)e.getSender();
		if ((sender != null) && (sender.getPendingConnection() != null))
		{
			mute = Punish.getMuteInfo(e.getSender().toString());
			if (mute != null)
			{
				if (Punish.checkMute(mute))
				{
					if (e.isCommand())
					{
						if (blockedCMDs.contains(e.getMessage().split(" ")[0].toLowerCase()))
						{
							if (mute.getMuted_Until() == null)
							{
								sender.sendMessage(new TextComponent(Punish.getPermMuteMSG(mute.getActor(), mute.getReason())));
								e.setCancelled(true);
							}
							else
							{
								SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
								sender.sendMessage(new TextComponent(Punish.getTempMuteMSG(mute.getActor(), mute.getReason(), sdf.format(mute.getMuted_Until()))));
								e.setCancelled(true);
							}
						}
					}
					else
					{
						if (mute.getMuted_Until() == null)
						{
							sender.sendMessage(new TextComponent(Punish.getPermMuteMSG(mute.getActor(), mute.getReason())));
							e.setCancelled(true);
						}
						else
						{
							SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
							sender.sendMessage(new TextComponent(Punish.getTempMuteMSG(mute.getActor(), mute.getReason(), sdf.format(mute.getMuted_Until()))));
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void checkBan(LoginEvent e)
	{
		Ban ban = Punish.getBanInfo(e.getConnection().getName());
		if (ban != null)
		{
			if (Punish.checkBan(ban))
			{
				e.setCancelled(true);
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
				if (ban.getBanned_Until() == null)
				{
					e.setCancelReason(Punish.getPermaBanMSG(ban.getActor(), ban.getReason()));
					main.getUtilLogger().info("Punish> " + e.getConnection().getName() + "'s connection was refused. Reason: [Banned] by " + ban.getActor() + " for " + ban.getReason() + ".");
				}
				else
				{
					e.setCancelReason(Punish.getTempBanMSG(ban.getActor(), ban.getReason(), sdf.format(ban.getBanned_Until())));
					main.getUtilLogger().info("Punish> " + e.getConnection().getName() + "'s connection was refused. Reason: [Tempbanned] by " + ban.getActor() + " for " + ban.getReason() + ".");
				}
			}
		}
	}
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e)
	{
		if (e.isCancelled())
		{
			return;
		}
		if (!(e.getSender() instanceof Server))
		{
			return;
		}
		if (!e.getTag().equals("TevoNetworkOutgoing"))
		{
			return;
		}
		e.setCancelled(true);
		Server server = (Server)e.getSender();
		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		String subchannel = in.readUTF();
		if (subchannel.equals("NetworkBroadcast"))
		{
			ChatManager.sendNetworkBroadcast(in.readUTF(), server);
		}
		if (subchannel.equals("StaffChat"))
		{
			ChatManager.sendStaffChat(in.readUTF(), in.readUTF(), server);
		}
		if (subchannel.equals("GlobalChat"))
		{
			ChatManager.sendGlobalChat(in.readUTF(), in.readUTF(), server);
		}
		if (subchannel.equals("PrivateChat"))
		{
			ChatManager.sendPrivateMessage(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), server);
		}
		if (subchannel.equals("Ban"))
		{
			Punish.banPlayer(in.readUTF(), in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("Tempban"))
		{
			Punish.tempbanPlayer(in.readUTF(), in.readUTF(), in.readUTF(), in.readInt(), in.readInt(), in.readInt());
		}
		if (subchannel.equals("Unban"))
		{
			Punish.unbanPlayer(in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("Mute"))
		{
			Punish.mutePlayer(in.readUTF(), in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("Tempmute"))
		{
			Punish.tempmutePlayer(in.readUTF(), in.readUTF(), in.readUTF(), in.readInt(), in.readInt(), in.readInt());
		}
		if (subchannel.equals("Unmute"))
		{
			Punish.unmutePlayer(in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("Kick"))
		{
			Punish.kickPlayer(in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("KickDefault"))
		{
			Punish.kickPlayer(in.readUTF());
		}
		if (subchannel.equals("PunishInfo"))
		{
			Punish.punishInfo(in.readUTF(), in.readUTF());
		}
		if (subchannel.equals("StaffNotification"))
		{
			ChatManager.sendStaffNotification(in.readUTF());
		}
	}
	
	
}
