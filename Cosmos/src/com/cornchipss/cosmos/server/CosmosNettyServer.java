package com.cornchipss.cosmos.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.cornchipss.cosmos.game.ServerGame;
import com.cornchipss.cosmos.netty.PacketTypes;
import com.cornchipss.cosmos.netty.packets.Packet;
import com.cornchipss.cosmos.server.command.CommandHandler;
import com.cornchipss.cosmos.utils.Utils;

public class CosmosNettyServer implements Runnable
{
	private volatile boolean running;
	
	private final ServerGame game;
	
	private CommandHandler cmdHandler;
	
	private ServerPlayerList players = new ServerPlayerList();
	
	private DatagramSocket socket;
	
	public CosmosNettyServer(ServerGame game, CommandHandler cmdHandler)
	{
		running = true;
		this.game = game;
		
		this.cmdHandler = cmdHandler;
	}
	
	private synchronized void process(DatagramPacket packet, DatagramSocket serverSocket) throws IOException
	{
		ServerClient client = new ServerClient(packet.getAddress(), packet.getPort());
		
		byte[] buffer = packet.getData();
		
		byte marker = Packet.findMarker(buffer, packet.getOffset(), packet.getLength());
		
		Packet p = PacketTypes.packet(marker);
		
		if(p == null)
		{
			Utils.println("INVALID PACKET TYPE");
			buffer[0] = -1; // we can reuse the same buffer
			client.send(buffer, 1, this);
			return;
		}
		
		int off = Packet.additionalOffset(buffer, packet.getOffset(), packet.getLength());
		
		p.onReceiveServer(buffer, packet.getLength() - off, packet.getOffset() + off, client, this);
	}
	
	@Override
	public void run()
	{
		try
		{
			socket = new DatagramSocket(1337);
		
			byte[] buffer = new byte[1024];
			
			while(running)
			{
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				
				process(packet, socket);
			}
			
			socket.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public boolean running() { return running; }
	public void running(boolean r) { running = r; }
	
	public CommandHandler commandHandler() { return cmdHandler; }
	public void commandHandler(CommandHandler h) { this.cmdHandler = h; }
	
	public ServerGame game() { return game; }

	public ServerPlayerList players() { return players; }

	public DatagramSocket socket() { return socket; }
}
