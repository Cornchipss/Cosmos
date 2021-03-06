package com.cornchipss.cosmos.structures;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.cornchipss.cosmos.blocks.Block;
import com.cornchipss.cosmos.utils.Maths;
import com.cornchipss.cosmos.utils.Utils;
import com.cornchipss.cosmos.world.World;
import com.cornchipss.cosmos.world.entities.player.Player;

/**
 * A structure representing a ship
 */
public class Ship extends Structure
{
	private final static int MAX_DIMENSIONS = 16 * 10;
	
	private Player pilot;
	
	private Vector3f corePos = new Vector3f();
	
	public Ship(World world, int id)
	{
		super(world, MAX_DIMENSIONS, MAX_DIMENSIONS, MAX_DIMENSIONS, id);
	}
	
	public Vector3fc corePosition()
	{
		corePos.set(MAX_DIMENSIONS / 2.f, MAX_DIMENSIONS / 2.f, MAX_DIMENSIONS / 2.f);
		return corePos;
	}
	
	@Override
	public void block(int x, int y, int z, Block b)
	{
		super.block(x, y, z, b);
	}
	
	@Override
	public void update(float delta)
	{
		super.update(delta);
		
		if(pilot == null)
			body().velocity(body().velocity().mul(0.99f)); // no more drifting into space once the pilot leaves
		else
		{
			pilot.body().velocity(Maths.zero());
			pilot.body().transform().position(localCoordsToWorldCoords(width()/2, height()/2, length()/2));
			
			pilot.body().transform().orientation(body().transform().orientation());
		}
	}

	public void setPilot(Player p)
	{
		if(!Utils.equals(pilot, p))
		{
			if(pilot != null)
				pilot.shipPiloting(null);
			
			pilot = p;
			if(p != null)
				p.shipPiloting(this);
		}
	}
	
	public Player pilot()
	{
		return pilot;
	}
}
