package me.thrusmyster.tevobungee.Punish;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import me.thrusmyster.tevobungee.TevoBungee;
import me.thrusmyster.tevobungee.Chat.ChatManager;
import me.thrusmyster.tevobungee.Punish.Objects.Ban;
import me.thrusmyster.tevobungee.Punish.Objects.Mute;
import me.thrusmyster.tevobungee.SQL.SQLManager;
import me.thrusmyster.tevobungee.Util.CC;
import me.thrusmyster.tevobungee.Util.UUIDFetcher;

public class Punish {

	private static TevoBungee main = TevoBungee.getInstance();
	private static SQLManager sql = main.getPunishSQlManager();
	private static SQLManager mainsql = main.getSQLManager();
	private static String column_UUID = "UUID";
	private static String column_Name = "Name";
	private static String column_Bans = "Bans";
	private static String column_Mutes = "Mutes";
	private static String column_Actor = "Actor";
	private static String column_Reason = "Reason";
	private static String column_Banned = "Banned";
	private static String column_Muted = "Muted";
	private static String column_Banned_Until = "Banned_Until";
	private static String column_Muted_Until = "Muted_Until";
	private static String table_Bans = "Bans";
	private static String table_Mutes = "Mutes";
	private static String table_TempBans = "TempBans";
	private static String table_TempMutes = "TempMutes";
	private static String table_History = "History";
	private static String table_OPS = "OPS";
	public static String prefix_Punish = "§8[§2§lT§4§lN§7>§9Punish§8] ";

	public static void banPlayer(String player, String actor, String reason) {
		String playeruuid = getSQLPlayerUUID(player);
		if (playeruuid != null) {
			try {
				if (mainsql.existanceQuery("SELECT " + column_UUID + " FROM " + table_OPS + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnError + "You cannot ban an operator! Abuse of the punishment system will result in you being removed from the staff team and possibly banned!"));
					}
					return;
				}
				if (!valueExistance(column_UUID, table_Bans, playeruuid)) {
					if (valueExistance(column_UUID, table_TempBans, playeruuid)) {
						removeValue(table_TempBans, column_UUID, playeruuid);
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar calnow = Calendar.getInstance();
					Date now = new Date(calnow.getTimeInMillis());
					setValue(table_Bans, column_UUID + "," + column_Name + "," + column_Actor + "," + column_Reason + "," + column_Banned,
							"'" + playeruuid + "','" + player + "','" + actor + "','" + reason + "','" + sdf.format(now) + "'");
					ProxiedPlayer banned = main.getProxyServer().getPlayer(player);
					if (banned != null) {
						banned.disconnect(TextComponent.fromLegacyText(getPermaBanMSG(actor, reason)));
					}
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been permanently banned for " + CC.cWhite + reason + CC.end));
					}
					updateHistory(player, column_Bans, 1);
					main.getUtilLogger().info("Punish> " + player + " has been permanently banned for " + reason + " by " + actor + ".");
					ChatManager.sendStaffNotification(getStaffNote("Ban", player, reason, "never", actor));
				}
				else {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is already banned!"));
					}
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else {
			ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void tempbanPlayer(String player, String actor, String reason, int days, int hours, int minutes) {
		String playeruuid = getSQLPlayerUUID(player);
		if (playeruuid != null) {
			try {
				if (mainsql.existanceQuery("SELECT " + column_UUID + " FROM " + table_OPS + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnError + "You cannot ban an operator! Abuse of the punishment system will result in you being removed from the staff team and possibly banned!"));
					}
					return;
				}
				if ((!valueExistance(column_UUID, table_TempBans, playeruuid)) && (!valueExistance(column_UUID, table_Bans, playeruuid))) {
					Calendar cal = Calendar.getInstance();
					cal.add(5, days);
					cal.add(11, hours);
					cal.add(12, minutes);
					Date until = new Date(cal.getTimeInMillis());
					SimpleDateFormat sdf = new SimpleDateFormat();
					sdf.applyPattern("dd MMM yyyy HH:mm:ss");
					String chatuntil = sdf.format(until);
					sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
					Calendar calnow = Calendar.getInstance();
					Date now = new Date(calnow.getTimeInMillis());
					setValue(table_TempBans, column_UUID + "," + column_Name + "," + column_Actor + "," + column_Reason + "," + column_Banned + "," + column_Banned_Until,
							"'" + playeruuid + "','" + player + "','" + actor + "','" + reason + "','" + sdf.format(now) + "','" + sdf.format(until) + "'");
					ProxiedPlayer banned = main.getProxyServer().getPlayer(player);
					if (banned != null) {
						banned.disconnect(TextComponent.fromLegacyText(getTempBanMSG(actor, reason, chatuntil)));
					}
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been banned for " + CC.cWhite + reason + CC.tnInfo + " until " + CC.tnValue + chatuntil + CC.end));
					}
					updateHistory(player, column_Bans, 1);
					main.getUtilLogger().info("Punish> " + player + " has been banned for " + reason + " by " + actor + " until " + chatuntil + ".");
					ChatManager.sendStaffNotification(getStaffNote("Ban", player, reason, chatuntil, actor));
				}
				else {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is already banned!"));
					}
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else {
			ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void mutePlayer(String player, String actor, String reason) {
		String playeruuid = getSQLPlayerUUID(player);
		if (playeruuid != null) {
			try {
				if (mainsql.existanceQuery("SELECT " + column_UUID + " FROM " + table_OPS + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnError + "You cannot mute an operator! Abuse of the punishment system will result in you being removed from the staff team and possibly banned!"));
					}
					return;
				}
				if ((!valueExistance(column_UUID, table_Mutes, playeruuid)) && (!valueExistance(column_UUID, table_Bans, playeruuid))) {
					if (valueExistance(column_UUID, table_TempMutes, playeruuid)) {
						removeValue(table_TempMutes, column_UUID, playeruuid);
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar calnow = Calendar.getInstance();
					Date now = new Date(calnow.getTimeInMillis());
					setValue(table_Mutes, column_UUID + "," + column_Name + "," + column_Actor + "," + column_Reason + "," + column_Muted,
							"'" + playeruuid + "','" + player + "','" + actor + "','" + reason + "','" + sdf.format(now) + "'");
					ProxiedPlayer muted = main.getProxyServer().getPlayer(player);
					if (muted != null) {
						muted.sendMessage(TextComponent.fromLegacyText(getPermMuteMSG(actor, reason)));
						;
					}
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been permanently muted for " + CC.cWhite + reason + CC.end));
					}
					updateHistory(player, column_Mutes, 1);
					main.getUtilLogger().info("Punish> " + player + " has been permanently muted for " + reason + " by " + actor + ".");
					ChatManager.sendStaffNotification(getStaffNote("Mute", player, reason, "never", actor));
				}
				else {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is already banned/muted!"));
					}
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else {
			ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void tempmutePlayer(String player, String actor, String reason, int days, int hours, int minutes) {
		String playeruuid = getSQLPlayerUUID(player);
		if (playeruuid != null) {
			try {
				if (mainsql.existanceQuery("SELECT " + column_UUID + " FROM " + table_OPS + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnError + "You cannot mute an operator! Abuse of the punishment system will result in you being removed from the staff team and possibly banned!"));
					}
					return;
				}
				if ((!valueExistance(column_UUID, table_TempMutes, playeruuid)) && (!valueExistance(column_UUID, table_Mutes, playeruuid)) && (!valueExistance(column_UUID, table_Bans, playeruuid))) {
					Calendar cal = Calendar.getInstance();
					cal.add(5, days);
					cal.add(11, hours);
					cal.add(12, minutes);
					Date until = new Date(cal.getTimeInMillis());
					SimpleDateFormat sdf = new SimpleDateFormat();
					sdf.applyPattern("dd MMM yyyy HH:mm:ss");
					String chatuntil = sdf.format(until);
					sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
					Calendar calnow = Calendar.getInstance();
					Date now = new Date(calnow.getTimeInMillis());
					setValue(table_TempMutes, column_UUID + "," + column_Name + "," + column_Actor + "," + column_Reason + "," + column_Muted + "," + column_Muted_Until,
							"'" + playeruuid + "','" + player + "','" + actor + "','" + reason + "','" + sdf.format(now) + "','" + sdf.format(until) + "'");
					ProxiedPlayer muted = main.getProxyServer().getPlayer(player);
					if (muted != null) {
						muted.sendMessage(TextComponent.fromLegacyText(getTempMuteMSG(actor, reason, chatuntil)));
					}
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(
								prefix_Punish + CC.tnPlayer + player + " " + CC.tnInfo + "has been muted for " + CC.cWhite + reason + " " + CC.tnInfo + "until " + CC.tnValue + chatuntil + CC.end));
					}
					updateHistory(player, column_Mutes, 1);
					main.getUtilLogger().info("Punish> " + player + " has been banned for " + reason + " by " + actor + " until " + chatuntil + ".");
					ChatManager.sendStaffNotification(getStaffNote("Mute", player, reason, chatuntil, actor));
				}
				else {
					ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
					if (sender != null) {
						sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is already muted/banned!"));
					}
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else {
			ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void unmutePlayer(String player, String actor) {
		String playeruuid = getSQLPlayerUUID(player);
		ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
		if (playeruuid != null) {
			if (valueExistance(column_UUID, table_Mutes, playeruuid)) {
				removeValue(table_Mutes, column_UUID, playeruuid);
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been unmuted."));
				}
				ChatManager.sendStaffNotification(getStaffNoteRevoke("Unmute", player, actor));
				main.getUtilLogger().info("Punish> " + player + " has been unmuted by " + actor + ".");
			}
			else if (valueExistance(column_UUID, table_TempMutes, playeruuid)) {
				removeValue(table_TempMutes, column_UUID, playeruuid);
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been unmuted."));
				}
				ChatManager.sendStaffNotification(getStaffNoteRevoke("Unmute", player, actor));
				main.getUtilLogger().info("Punish> " + player + " has been unmuted by " + actor + ".");
			}
			else {
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is not muted!"));
				}
			}
		}
		else {
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void unbanPlayer(String player, String actor) {
		String playeruuid = getSQLPlayerUUID(player);
		ProxiedPlayer sender = main.getProxyServer().getPlayer(actor);
		if (playeruuid != null) {
			if (valueExistance(column_UUID, table_Bans, playeruuid)) {
				removeValue(table_Bans, column_UUID, playeruuid);
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been unbanned."));
				}
				ChatManager.sendStaffNotification(getStaffNoteRevoke("Unban", player, actor));
				main.getUtilLogger().info("Punish> " + player + " has been unbanned by " + actor + ".");
			}
			else if (valueExistance(column_UUID, table_TempBans, playeruuid)) {
				removeValue(table_TempBans, column_UUID, playeruuid);
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnInfo + " has been unbanned."));
				}
				ChatManager.sendStaffNotification(getStaffNoteRevoke("Unban", player, actor));
				main.getUtilLogger().info("Punish> " + player + " has been unbanned by " + actor + ".");
			}
			else {
				if (sender != null) {
					sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is not banned!"));
				}
			}
		}
		else {
			if (sender != null) {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static void kickPlayer(String player) {
		ProxiedPlayer p = main.getProxyServer().getPlayer(player);
		if (p != null) {
			p.disconnect(TextComponent.fromLegacyText("§8[§2§lT§4§lN§7>§9Server§8] §7§lYou §c§lhave been kicked!"));
			main.getUtilLogger().info("Punish> " + player + " has been kicked. No reason was specified.");
			ChatManager.sendStaffNotification(CC.tnPlayer + player + CC.tnInfo + " has been kicked. No reason was specified.");
		}
	}

	public static void kickPlayer(String player, String reason) {
		ProxiedPlayer p = main.getProxyServer().getPlayer(player);
		if (p != null) {
			p.disconnect(TextComponent.fromLegacyText("§8[§2§lT§4§lN§7>§9Server§8] §7§lYou §c§lhave been kicked for \n " + CC.cWhite + reason + "§c§l."));
			main.getUtilLogger().info("Punish> " + player + " has been kicked for " + reason + ".");
			ChatManager.sendStaffNotification(CC.tnPlayer + player + CC.tnInfo + " has been kicked for " + CC.cWhite + reason + CC.end);
		}
	}

	public static void punishInfo(String requester, String player) {
		ProxiedPlayer sender = main.getProxyServer().getPlayer(requester);
		if (sender != null) {
			String playeruuid = getSQLPlayerUUID(player);
			if (playeruuid != null) {
				int bans = getHistory(player, column_Bans);
				int mutes = getHistory(player, column_Mutes);
				Mute currentmute = getMuteInfo(player);
				Ban currentban = getBanInfo(player);
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnHead + "Punish Information for " + CC.tnPlayer + player + CC.tnDiv + ":"));
				sender.sendMessage(TextComponent.fromLegacyText(CC.tnInfo + "Name: " + CC.tnPlayer + player));
				sender.sendMessage(TextComponent.fromLegacyText(CC.tnInfo + "Bans: " + CC.tnValue + bans));
				sender.sendMessage(TextComponent.fromLegacyText(CC.tnInfo + "Mutes: " + CC.tnValue + mutes));
				sender.sendMessage(TextComponent.fromLegacyText(CC.tnInfo + "Current Punishments:"));
				sender.sendMessage(TextComponent.fromLegacyText(" "));
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
				if (currentban != null) {
					if (currentban.getBanned_Until() == null) {
						sender.sendMessage(TextComponent.fromLegacyText(CC.tnDiv + "- " + CC.tnValue + "[Ban] " + CC.tnInfo + "Actor: " + CC.tnPlayer + currentban.getActor() + CC.tnInfo + " Reason: "
								+ CC.cWhite + currentban.getReason() + CC.tnInfo + " Until: " + CC.tnValue + "Never"));
					}
					else {
						sender.sendMessage(TextComponent.fromLegacyText(CC.tnDiv + "- " + CC.tnValue + "[Ban] " + CC.tnInfo + "Actor: " + CC.tnPlayer + currentban.getActor() + CC.tnInfo + " Reason: "
								+ CC.cWhite + currentban.getReason() + CC.tnInfo + " Until: " + CC.tnValue + sdf.format(currentban.getBanned_Until())));
					}
				}
				else {
					sender.sendMessage(TextComponent.fromLegacyText("               " + CC.tnError + "No Current Bans"));
				}
				sender.sendMessage(TextComponent.fromLegacyText(" "));
				if (currentmute != null) {
					if (currentmute.getMuted_Until() == null) {
						sender.sendMessage(TextComponent.fromLegacyText(CC.tnDiv + "- " + CC.tnValue + "[Mute] " + CC.tnInfo + "Actor: " + CC.tnPlayer + currentmute.getActor() + CC.tnInfo
								+ " Reason: " + CC.cWhite + currentmute.getReason() + CC.tnInfo + " Until: " + CC.tnValue + "Never"));
					}
					else {
						sender.sendMessage(TextComponent.fromLegacyText(CC.tnDiv + "- " + CC.tnValue + "[Mute] " + CC.tnInfo + "Actor: " + CC.tnPlayer + currentmute.getActor() + CC.tnInfo
								+ " Reason: " + CC.cWhite + currentmute.getReason() + CC.tnInfo + " Until: " + CC.tnValue + sdf.format(currentmute.getMuted_Until())));
					}
				}
				else {
					sender.sendMessage(TextComponent.fromLegacyText("               " + CC.tnError + "No Current Mutes"));
				}
				sender.sendMessage(TextComponent.fromLegacyText(" "));
				sender.sendMessage(TextComponent.fromLegacyText(CC.cD_Gray + CC.fStrike + CC.fBold + "•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••"));
			}
			else {
				sender.sendMessage(TextComponent.fromLegacyText(prefix_Punish + CC.tnPlayer + player + CC.tnError + " is an invalid player!"));
			}
		}
	}

	public static String getStaffNoteRevoke(String type, String player, String actor) {
		return CC.tnInfo + "Revoked punishment: " + CC.tnValue + "[" + type + "] " + CC.tnInfo + "targeting " + CC.tnPlayer + player + CC.tnInfo + ". Actor: " + CC.tnPlayer + actor + CC.end;
	}

	public static String getStaffNote(String type, String target, String reason, String until, String actor) {
		return CC.tnInfo + "New punishment: " + CC.tnValue + "[" + type + "] " + CC.tnInfo + "targeting " + CC.tnPlayer + target + CC.tnInfo + " for " + CC.cWhite + reason + CC.tnInfo + " until "
				+ CC.tnValue + until + CC.tnInfo + " Issuer: " + CC.tnPlayer + actor + CC.end;
	}

	public static String getTempBanMSG(String actor, String reason, String until) {
		return prefix_Punish + CC.tnPlayer + CC.fBold + "You " + CC.tnBError + "have been banned by " + CC.tnPlayer + CC.fBold + actor + CC.tnBError + " for " + CC.cWhite + reason + CC.tnBError
				+ " until " + CC.tnValue + until + CC.tnBError + ".\n" + CC.tnInfo + "Any appeals made for this type of ban will be ignored.";
	}

	public static String getPermaBanMSG(String actor, String reason) {
		return prefix_Punish + CC.tnPlayer + CC.fBold + "You " + CC.tnBError + "have been permanently banned by " + CC.tnPlayer + CC.fBold + actor + CC.tnBError + " for " + CC.cWhite + reason
				+ CC.tnBError + ".\n" + CC.tnInfo + "Think you have been banned unfairly? \n" + CC.tnInfo + "Make an appeal at " + CC.tnValue + "http://www.tevonetwork.com" + CC.end;
	}

	public static String getPermMuteMSG(String actor, String reason) {
		return prefix_Punish + CC.tnPlayer + "You " + CC.tnError + "have been permanently §cmuted by " + CC.tnPlayer + actor + CC.tnInfo + " for " + CC.cWhite + reason + CC.tnError + "." + CC.tnInfo
				+ " Think you have §ebeen unfairly muted? §eMake an appeal at " + CC.tnValue + "http://www.tevonetwork.com";
	}

	public static String getTempMuteMSG(String actor, String reason, String until) {
		return prefix_Punish + CC.tnPlayer + "You " + CC.tnError + "have been muted by " + CC.tnPlayer + actor + CC.tnError + " for " + CC.cWhite + reason + CC.tnError + " until " + CC.tnValue + until
				+ CC.tnError + ".";
	}

	public static Ban getBanInfo(String player) {
		Ban ban = null;
		String playeruuid = getSQLPlayerUUID(player);
		if (valueExistance(column_UUID, table_Bans, playeruuid)) {
			ResultSet set = getValueSet(table_Bans, column_UUID, playeruuid);
			try {
				while (set.next()) {
					ban = new Ban(set.getString(column_Name), set.getString(column_Reason), set.getString(column_Actor), set.getTimestamp(column_Banned));
				}
				set.close();
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else if (valueExistance(column_UUID, table_TempBans, playeruuid)) {
			ResultSet set = getValueSet(table_TempBans, column_UUID, playeruuid);
			try {
				while (set.next()) {
					ban = new Ban(set.getString(column_Name), set.getString(column_Reason), set.getString(column_Actor), set.getTimestamp(column_Banned), set.getTimestamp(column_Banned_Until));
				}
				set.close();
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		return ban;
	}

	public static Mute getMuteInfo(String player) {
		Mute mute = null;
		String playeruuid = getSQLPlayerUUID(player);
		if (valueExistance(column_UUID, table_Mutes, playeruuid)) {
			ResultSet set = getValueSet(table_Mutes, column_UUID, playeruuid);
			try {
				while (set.next()) {
					mute = new Mute(set.getString(column_Name), set.getString(column_Reason), set.getString(column_Actor), set.getTimestamp(column_Muted));
				}
				set.close();
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		else if (valueExistance(column_UUID, table_TempMutes, playeruuid)) {
			ResultSet set = getValueSet(table_TempMutes, column_UUID, playeruuid);
			try {
				while (set.next()) {
					mute = new Mute(set.getString(column_Name), set.getString(column_Reason), set.getString(column_Actor), set.getTimestamp(column_Muted), set.getTimestamp(column_Muted_Until));
				}
				set.close();
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error!");
				e.printStackTrace();
			}
		}
		return mute;
	}

	public static boolean checkBan(Ban ban) {
		if (ban.isBanned()) {
			return true;
		}
		String playeruuid = getSQLPlayerUUID(ban.getPlayer());
		if (playeruuid != null) {
			removeValue(table_TempBans, column_UUID, playeruuid);
		}
		return false;
	}

	public static boolean checkMute(Mute mute) {
		if (mute.isMuted()) {
			return true;
		}
		String playeruuid = getSQLPlayerUUID(mute.getPlayer());
		if (playeruuid != null) {
			removeValue(table_TempMutes, column_UUID, playeruuid);
		}
		return false;
	}

	private static boolean valueExistance(String column, String table, String value) {
		try {
			if (sql.existanceQuery("SELECT " + column + " FROM " + table + " WHERE " + column + " ='" + value + "';")) {
				return true;
			}
		}
		catch (SQLException e) {
			main.getUtilLogger().warning("Punish> There was an sql error! (value existance)");
			e.printStackTrace();
		}
		return false;
	}

	private static ResultSet getValueSet(String table, String column, String value) {
		ResultSet set = null;
		try {
			set = sql.sqlQuery("SELECT * FROM " + table + " WHERE " + column + " ='" + value + "';");
		}
		catch (SQLException e) {
			main.getUtilLogger().warning("Punish> There was an sql error! (value set)");
			e.printStackTrace();
		}
		return set;
	}

	private static void setValue(String table, String columns, String values) {
		try {
			sql.standardQuery("INSERT INTO " + table + "(" + columns + ") VALUES(" + values + ");");
		}
		catch (SQLException e) {
			main.getUtilLogger().warning("Punish> There was an sql error! (set value)");
			e.printStackTrace();
		}
	}

	private static void updateHistory(String player, String column, int amount) {
		String playeruuid = getSQLPlayerUUID(player);
		if (playeruuid != null) {
			try {
				int current = 0;
				if (sql.existanceQuery("SELECT " + column_UUID + " FROM " + table_History + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ResultSet set = sql.sqlQuery("SELECT " + column + " FROM " + table_History + " WHERE " + column_UUID + " ='" + playeruuid + "';");
					if ((set != null) && (set.next())) {
						current = set.getInt(1);
					}
					int newamount = current + amount;
					sql.standardQuery("UPDATE " + table_History + " SET " + column + " = " + newamount + ", " + column_Name + " = '" + player + "' WHERE " + column_UUID + " ='" + playeruuid + "';");
				}
				else {
					sql.standardQuery("INSERT INTO " + table_History + " (" + column_UUID + "," + column_Name + "," + column_Bans + "," + column_Mutes + ") VALUES('" + playeruuid + "','" + player
							+ "'," + current + "," + current + ");");
					ResultSet set = sql.sqlQuery("SELECT " + column + " FROM " + table_History + " WHERE " + column_UUID + " ='" + playeruuid + "';");
					if ((set != null) && (set.next())) {
						current = set.getInt(1);
					}
					sql.standardQuery(
							"UPDATE " + table_History + " SET " + column + " = " + current + amount + ", " + column_Name + " = '" + player + "' WHERE " + column_UUID + " ='" + playeruuid + "';");
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error! (set value)");
				e.printStackTrace();
			}
		}
	}

	private static int getHistory(String player, String column) {
		String playeruuid = getSQLPlayerUUID(player);
		int current = 0;
		if (playeruuid != null) {
			try {
				if (sql.existanceQuery("SELECT " + column_UUID + " FROM " + table_History + " WHERE " + column_UUID + " ='" + playeruuid + "';")) {
					ResultSet set = sql.sqlQuery("SELECT " + column + " FROM " + table_History + " WHERE " + column_UUID + " ='" + playeruuid + "';");
					if ((set != null) && (set.next())) {
						current = set.getInt(1);
					}
				}
			}
			catch (SQLException e) {
				main.getUtilLogger().warning("Punish> There was an sql error! (set value)");
				e.printStackTrace();
			}
		}
		return current;
	}

	private static void removeValue(String table, String column, String value) {
		try {
			sql.standardQuery("DELETE FROM " + table + " WHERE " + column + " ='" + value + "';");
		}
		catch (SQLException e) {
			main.getUtilLogger().warning("Punish> There was an sql error! (remove value)");
			e.printStackTrace();
		}
	}

	private static String getSQLPlayerUUID(String player) {
		UUID uuid = null;
		if (main.getProxyServer().getPlayer(player) != null) {
			uuid = main.getProxyServer().getPlayer(player).getUniqueId();
		}
		else {
			uuid = UUIDFetcher.getUUID(player);
		}
		if (uuid != null) {
			return uuid.toString().replace("-", "");
		}
		return null;
	}

}
