package me.thrusmyster.tevobungee.Networking;

import net.md_5.bungee.api.config.ServerInfo;

public class TevoNetworkMessageTask implements Runnable{

	private final String channel = "TevoNetworkIncoming";
	private final byte[] bytes;
	private final ServerInfo server;
	
	public TevoNetworkMessageTask(ServerInfo server, byte[] bytes)
	{
		this.server = server;
		this.bytes = bytes;
	}
	
	@Override
	public void run() 
	{
		this.server.sendData(channel, bytes);
	}
}	
