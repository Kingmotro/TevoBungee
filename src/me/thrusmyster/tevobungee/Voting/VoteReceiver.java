package me.thrusmyster.tevobungee.Voting;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.crypto.BadPaddingException;

import me.thrusmyster.tevobungee.TevoBungee;
import me.thrusmyster.tevobungee.Voting.crypto.RSA;

public class VoteReceiver implements Runnable {

	private final String host;
	private final int port;
	private ServerSocket serversocket;
	private boolean running = true;
	
	public VoteReceiver(String host, int port) 
	throws Exception
	{
		this.host = host;
		this.port = port;
		
		initialize();
	}

	private void initialize()
	throws Exception
	{
		try
		{
			this.serversocket = new ServerSocket();
			this.serversocket.bind(new InetSocketAddress(this.host, this.port));
		}
		catch(Exception e)
		{
			TevoBungee.getInstance().getUtilLogger().warning("Vote> Failed to create Server socket!");
		}
	}
	
	public void shutdown()
	{
		this.running = false;
		if (this.serversocket == null)
		{
			return;
		}
		try
		{
			this.serversocket.close();
		}
		catch(Exception e)
		{
			TevoBungee.getInstance().getUtilLogger().warning("Vote> Reciever did not shut down cleanly!");
		}
	}
	
	public void run()
	{
		while(this.running)
		{
			try
			{
				Socket socket = serversocket.accept();
				socket.setSoTimeout(3000);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				InputStream in = socket.getInputStream();
					
				writer.write("VOTIFIER 1.9");
				writer.newLine();
				writer.flush();
					
				byte[] block = new byte[256];
				in.read(block, 0, block.length);
					
				block = RSA.decrypt(block, TevoBungee.getInstance().getKeyPair().getPrivate());
					
				int position = 0;
					
				String opcode = readString(block, position);
				
				position += opcode.length() + 1;
				if (!opcode.equals("VOTE"))
				{
					throw new Exception("Vote> Unable to decode RSA!");
				}
					
				String servicename = readString(block, position);
				position += servicename.length() + 1;
				String username = readString(block, position);
				position += username.length() + 1;
				String address = readString(block, position);
				position += address.length() + 1;
				String timeStamp = readString(block, position);
				position += timeStamp.length() + 1;
					
				final Vote vote = new Vote();
				vote.setServiceName(servicename);
				vote.setAddress(address);
				vote.setUsername(username);
				vote.setTimeStamp(timeStamp);
					
				TevoBungee.getInstance().getUtilLogger().info("Vote> Vote record received: " + vote);
					
				for (VoteListener listeners : TevoBungee.getInstance().getListeners())
				{
					try
					{
						listeners.voteMade(vote);
					}
					catch (Exception e)
					{
						String listenername = listeners.getClass().getSimpleName();
						TevoBungee.getInstance().getUtilLogger().warning("Vote> Failed to pass vote to " + listenername);
						e.printStackTrace();
					}
				}
					
				writer.close();
				in.close();
				socket.close();
					
			}
			catch(SocketException se)
			{
				TevoBungee.getInstance().getUtilLogger().warning("Vote> Protocol Error! Ignoring Packet." + se.getLocalizedMessage());
			}
			catch (BadPaddingException bpe)
			{
				TevoBungee.getInstance().getUtilLogger().warning("Vote> Unable to decrypt vote record. Check public key!" + bpe.getLocalizedMessage());
			}
			catch (Exception e)
			{
				TevoBungee.getInstance().getUtilLogger().warning("Vote> Exception occured when receiving vote record!");
			}
		}
	}
	
	private String readString(byte[] data, int offset)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < data.length; i++)
		{
			if (data[i] == 10)
			{
				break;
			}
			builder.append((char)data[i]);
		}
		return builder.toString();
	}
	
}
