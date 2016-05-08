package me.thrusmyster.tevobungee.Punish.Objects;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class Ban {

	private String player;
	private String reason;
	private String actor;
	private Date banned;
	private Date banned_until;
	
	public Ban(String player, String reason, String actor, Timestamp banned)
	{
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.banned = banned;
	}
	
	public Ban(String player, String reason, String actor, Timestamp banned, Timestamp banned_until)
	{
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.banned = banned;
		this.banned_until = banned_until;
	}
	
	public String getPlayer()
	{
		return this.player;
	}
	
	public String getActor()
	{
		return this.actor;
	}
	
	public String getReason()
	{
		return this.reason;
	}
	
	public Date getBannedDate()
	{
		return this.banned;
	}
	
	public Date getBanned_Until()
	{
		return this.banned_until;
	}
	
	public boolean isBanned()
	{
		if (this.banned_until == null)
		{
			return true;
		}
		Date today = new Date(Calendar.getInstance().getTimeInMillis());
		return today.compareTo(this.banned_until) < 0;
	}
	
}
