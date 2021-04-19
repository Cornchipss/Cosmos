package com.cornchipss.cosmos.server;

import com.cornchipss.cosmos.cameras.Camera;
import com.cornchipss.cosmos.cameras.GimbalLockCamera;
import com.cornchipss.cosmos.world.World;
import com.cornchipss.cosmos.world.entities.player.Player;

public class ServerPlayer extends Player
{
	private ServerClient serverClient;
	
	public ServerPlayer(World world, ServerClient serverClient, String name)
	{
		super(world, name);
		
		this.serverClient = serverClient;
	}

	@Override
	public void update(float delta)
	{
		
	}

	@Override
	public Camera camera()
	{
		return new GimbalLockCamera(this);
	}
	
	public ServerClient client() { return serverClient; }
}
