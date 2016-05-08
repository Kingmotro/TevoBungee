package me.thrusmyster.tevobungee.Voting;

public class Vote {

	private String servicename;
	private String username;
	private String address;
	private String timeStamp;
	
	
	public String toString()
	{
		return "Vote (From: " + this.servicename + " Username: " + this.username + " Address: " + this.address + " TimeStamp: " + this.timeStamp + ")";
	}
	
	public void setServiceName(String servicename)
	{
		this.servicename = servicename;
	}
	
	public String getServiceName()
	{
		return this.servicename;
	}
	
	public void setUsername(String username)
	{
		this.username = ((username.length() <= 16) ? username : username.substring(0, 16));
	}
	
	public String getUsername()
	{
		return this.username;
	}
	
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	public String getAddress()
	{
		return this.address;
	}
	
	public void setTimeStamp(String timestamp)
	{
		this.timeStamp = timestamp;
	}
	
	public String getTimeStamp()
	{
		return this.timeStamp;
	}
	
}
