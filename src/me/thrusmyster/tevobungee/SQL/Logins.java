package me.thrusmyster.tevobungee.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import me.thrusmyster.tevobungee.TevoBungee;
import me.thrusmyster.tevobungee.Chat.ChatManager;
import me.thrusmyster.tevobungee.Util.CC;
import me.thrusmyster.tevobungee.Util.UUIDFetcher;

public class Logins {
	
	private static TevoBungee main = TevoBungee.getInstance();
	private static SQLManager sql = main.getSQLManager();
	
	public static int getLogins(String playername)
	{
		int logins = 0;
		String uuid = getSQLUUID(playername);
		if (uuid != null)
		{
			try
			{
				if (sql.existanceQuery("SELECT UUID FROM Logins WHERE UUID = '" + uuid + "';"))
				{
					ResultSet set = sql.sqlQuery("SELECT Logins FROM Logins WHERE UUID = '" + uuid + "';");
					if ((set.next()) && (set != null))
					{
						logins = set.getInt(1);
					}
				}
			}
			catch(SQLException e)
			{
				main.getUtilLogger().warning("Logins> SQL failed!");
				e.printStackTrace();
			}
		}
		return logins;
	}
	
	public static void incrementLogins(String playername)
	{
		int logins = 0;
		String uuid = getSQLUUID(playername);
		if (uuid != null)
		{
			try
			{
				if (sql.existanceQuery("SELECT UUID FROM Logins WHERE UUID = '" + uuid + "';"))
				{
					ResultSet set = sql.sqlQuery("SELECT Logins FROM Logins WHERE UUID = '" + uuid + "';");
					if ((set.next()) && (set != null))
					{
						logins = set.getInt(1);
					}
					logins++;
					sql.standardQuery("UPDATE Logins SET Name = '" + playername + "',Logins = '" + String.valueOf(logins) + "' WHERE UUID = '" + uuid + "';");
				}
				else
				{
					logins++;
					sql.standardQuery("INSERT INTO Logins(UUID,Name,Logins) VALUES ('" + uuid + "','" + playername + "','" + String.valueOf(logins) + "');");
					ChatManager.sendStaffNotification(CC.tnPlayer + playername + CC.tnInfo + CC.fBold + " has joined for the first time!");
				}
			}
			catch(SQLException e)
			{
				main.getUtilLogger().warning("Logins> SQL failed!");
				e.printStackTrace();
			}
		}
	}
	
	private static String getSQLUUID(String playername)
	{
		UUID uuid = null;
		uuid = UUIDFetcher.getUUID(playername);
		if (uuid != null)
		{
			return uuid.toString().replace("-", "");
		}
		
		return null;
	}
}
