package me.thrusmyster.tevobungee.Util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

	public static String fromSecondstoDHMS(long total_seconds)
	{
		int days = (int)TimeUnit.SECONDS.toDays(total_seconds);
		long hours = TimeUnit.SECONDS.toHours(total_seconds) - (days * 24);
		long minutes = TimeUnit.SECONDS.toMinutes(total_seconds) - (TimeUnit.SECONDS.toHours(total_seconds) * 60);
		long seconds = TimeUnit.SECONDS.toSeconds(total_seconds) - (TimeUnit.SECONDS.toMinutes(total_seconds) * 60);
		
		String time = String.format("%d:%02d:%02d:%02d", hours, minutes, seconds);
		
		return time;
	}
	
	public static String fromSecondstoHMS(long total_seconds)
	{
		long hours = TimeUnit.SECONDS.toHours(total_seconds);
		long minutes = TimeUnit.SECONDS.toMinutes(total_seconds) - (TimeUnit.SECONDS.toHours(total_seconds) * 60);
		long seconds = TimeUnit.SECONDS.toSeconds(total_seconds) - (TimeUnit.SECONDS.toMinutes(total_seconds) * 60);
		
		String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
		return time;
	}
	
	public static String fromSecondstoMS(long total_seconds)
	{
		long minutes = TimeUnit.SECONDS.toMinutes(total_seconds) - (TimeUnit.SECONDS.toHours(total_seconds) * 60);
		long seconds = TimeUnit.SECONDS.toSeconds(total_seconds) - (TimeUnit.SECONDS.toMinutes(total_seconds) * 60);
		
		String time = String.format("%02d:%02d", minutes, seconds);
		
		return time;
	}
	
	public static boolean hasElapsed(long from, long required)
	{
	    return System.currentTimeMillis() - from > required;
	}
	
}
