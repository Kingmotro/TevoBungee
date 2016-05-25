package me.thrusmyster.tevobungee.Voting;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.thrusmyster.tevobungee.TevoBungee;
import me.thrusmyster.tevobungee.Networking.TevoNetworkMessageTask;
import me.thrusmyster.tevobungee.SQL.SQLManager;
import me.thrusmyster.tevobungee.Util.TimeUtils;
import me.thrusmyster.tevobungee.Util.UUIDFetcher;
import net.md_5.bungee.api.config.ServerInfo;

public class VoteManager implements Runnable{

	private static TevoBungee main = TevoBungee.getInstance();
	private static SQLManager sql = main.getSQLManager();
	private static Logger logger = main.getUtilLogger();
	private static HashMap<String, Date> last_vote = new HashMap<String, Date>();
	private static HashMap<String, Long> last_note = new HashMap<String, Long>();
	
	public static void addVote(String player)
	{
		UUID uuid = UUIDFetcher.getUUID(player);
		String sqluuid = uuid.toString().replace("-", "");
		try
		{
			Date votetime = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (sql.existanceQuery("SELECT UUID FROM Votes WHERE UUID ='" + sqluuid + "';"))
			{
				int currentCount = 0;
				ResultSet set = sql.sqlQuery("SELECT Count FROM Votes WHERE UUID ='" + sqluuid + "';");
				if ((set != null) && (set.next()))
				{
					currentCount = set.getInt(1);
				}
				currentCount++;
				sql.standardQuery("UPDATE Votes SET Count = '" + currentCount + "',Name = '" + player + "',Last = '" + sdf.format(votetime) + "' WHERE UUID = '" + sqluuid + "';");
			}
			else
			{
				sql.standardQuery("INSERT INTO Votes(UUID,Name,Count,Last) VALUES('" + sqluuid + "','" + player + "','" + 1 + "','" + sdf.format(votetime) + "')");
			}
			logger.info("Vote> 1 Vote count added to record of username: " + player + " UUID: " + sqluuid);
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Vote");
			out.writeUTF(player);
			out.writeInt(1);
			if (last_vote.containsKey(player.toLowerCase()))
			{
				last_vote.put(player.toLowerCase(), new Date(System.currentTimeMillis()));
			}
			for (ServerInfo server : main.getProxyServer().getServers().values())
			{
				if (server.getPlayers().size() > 0)
				{
					main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(server, out.toByteArray()));
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			logger.warning("Vote> Failed to add Vote count to record of username: " + player + " UUID: " + sqluuid);
		}
	}
	
	@Override
	public void run()
	{
		for (String name : last_vote.keySet())
		{
			Date vote = last_vote.get(name.toLowerCase());
			Calendar lastvote = Calendar.getInstance();
			Calendar ref = Calendar.getInstance();
			lastvote.setTime(vote);
			long hours = 0;
			while (ref.after(lastvote))
			{
				ref.add(Calendar.HOUR_OF_DAY, -1);
				hours++;
			}
			if (hours > 24)
			{
				if (last_note.containsKey(name.toLowerCase()))
				{
					if (TimeUtils.hasElapsed(last_note.get(name.toLowerCase()), 900000))
					{
						sendNotification(name);
					}
				}
				else
				{
					sendNotification(name);
				}
			}
		}
	}
	
	private static void sendNotification(String player)
	{
		last_note.put(player.toLowerCase(), System.currentTimeMillis());
		ByteArrayDataOutput out =  ByteStreams.newDataOutput();
		out.writeUTF("Vote");
		out.writeUTF(player);
		out.writeInt(0);
		for (ServerInfo server : main.getProxyServer().getServers().values())
		{
			if (server.getPlayers().size() > 0)
			{
				main.getProxyServer().getScheduler().runAsync(main, new TevoNetworkMessageTask(server, out.toByteArray()));
			}
		}
	}
	
	public static void addPlayer(String name)
	{
		UUID uuid = UUIDFetcher.getUUID(name);
		String sqluuid = uuid.toString().replace("-", "");
		try
		{
			Date lastvote = null;
			ResultSet set = sql.sqlQuery("SELECT Last FROM Votes WHERE UUID ='" + sqluuid + "';");
			if ((set != null) && (set.next()))
			{
				Timestamp stamp  = set.getTimestamp("Last");
				lastvote = new Date(stamp.getTime());
			}
			set.close();
			if (lastvote == null)
			{
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				last_vote.put(name.toLowerCase(), new Date(cal.getTimeInMillis()));
				return;
			}
			last_vote.put(name.toLowerCase(), lastvote);
			return;
		}
		catch (SQLException e)
		{
			logger.warning("VoteManager> SQL encountered an exception!");
			e.printStackTrace();
		}
	}
	
	public static void removePlayer(String name)
	{
		last_vote.remove(name.toLowerCase());
		last_note.remove(name.toLowerCase());
	}
	
}
