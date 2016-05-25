package me.thrusmyster.tevobungee.Punish.Objects;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class Mute {

	private String player;
	private String reason;
	private String actor;
	private Date muted;
	private Date muted_until;
	
	public Mute(String player, String reason, String actor, Timestamp muted)
	{
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.muted = muted;
	}
	
	public Mute(String player, String reason, String actor, Timestamp muted, Timestamp muted_until)
	{
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.muted = muted;
		this.muted_until = muted_until;
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
	
	public Date getMutedDate()
	{
		return this.muted;
	}
	
	public Date getMuted_Until()
	{
		return this.muted_until;
	}
	
	public boolean isMuted()
	{
		if (this.muted_until == null)
		{
			return true;
		}
		Date today = new Date(Calendar.getInstance().getTimeInMillis());
		return today.compareTo(this.muted_until) < 0;
	}
	
}
