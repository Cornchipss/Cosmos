package com.cornchipss.world.planet;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3i;

import com.cornchipss.registry.Blocks;
import com.cornchipss.rendering.Model;
import com.cornchipss.utils.ArrayListF;
import com.cornchipss.world.Block;
import com.cornchipss.world.sector.Sector;

/**
 * A body that can store blocks in a sector
 * @author Cornchip
 */
public class Planet
{
	/**
	 * The sector the planet is a part of (wow)
	 */
	private Sector sector;
	
	/**
	 * <p>Coordinate of the planet's center, relative to the sector's chunk coordinates</p>
	 * <p>So a planet coordinate of 0 would coordinate exactly to the top left corner of the sector</p>
	 */
	private float planetX, planetY, planetZ;
	
	/**
	 * Every block that makes up the planet
	 */
	private short[][][] blocks;
	
	/**
	 * Dimensions of the planet
	 */
	private int width, height, length;
	
	private Map<Model, ArrayListF> modelsList = new HashMap<>();
	private Map<Vector3i, Integer> positionsInArray = new HashMap<>();
	
	private boolean generated = false;
	
	/**
	 * Creates a body that can store blocks in a sector
	 * @param x The x position of the planet relative to the sector's chunk's positions
	 * @param y The y position of the planet relative to the sector's chunk's positions
	 * @param z The z position of the planet relative to the sector's chunk's positions
	 * @param width The width of the planet (in blocks) - MUST BE EVEN
	 * @param height The height of the planet (in blocks) - MUST BE EVEN
	 * @param length The length of the planet (in blocks) - MUST BE EVEN
	 */
	public Planet(int width, int height, int length)
	{
		if(width % 2 != 0 || height % 2 != 0 || length % 2 != 0)
			throw new IllegalArgumentException("Width, Height, and Length of any planet MUST be even!");
		
		generated = false;
		
		this.length = length;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Saves memory by not creating an array for every planet, even if the actual blocks haven't been generated/set yet.
	 * This is called whenever the {@link Planet#setBlock(int, int, int, boolean, short)} function is called.
	 */
	private void initBlocks()
	{
		generated = false;
		blocks = new short[length][height][width];
	}
	
	/**
	 * <p>Removes a block's model at a given coordinate from the list of models to render</p>
	 * <p>Remove a model before adding a model in the same spot!</p>
	 * <p>This is safe to call even if there was no model there before</p>
	 * @param x The x coordinate of the block to remove
	 * @param y The y coordinate of the block to remove
	 * @param z The z coordinate of the block to remove
	 */
	public void removeModel(int x, int y, int z)
	{
		Model model = getBlock(x, y, z).getModel();
		ArrayListF positions = modelsList.get(model);
		if(positions == null)
			return;
		
		if(positionsInArray.containsKey(new Vector3i(x, y, z)))
		{
			int i = positionsInArray.get(new Vector3i(x, y, z));
			int sizeToCopy = 3; // x,y,z
			
			for(int j = 0; j < sizeToCopy; j++)
				positions.remove(i + j);
		}
	}
	
	/**
	 * <p>Adds/Replaces a model to the list of models at a given coordinate, and makes sure there is no model already there.</p>
	 * <p>This automatically calls {@link Planet#removeModel(int, int, int)} before trying to add a new one</p>
	 * @param x The x coordinate of the block to add/replace
	 * @param y The y coordinate of the block to add/replace
	 * @param z The z coordinate of the block to add/replace
	 */
	public void updateModel(int x, int y, int z)
	{
		removeModel(x, y, z);
		
		boolean nonOpaqueFound = false;
		
		for(int dZ = -1; dZ <= 1; dZ++)
		{
			for(int dY = -1; dY <= 1; dY++)
			{
				for(int dX = -1; dX <= 1; dX++)
				{
					if(Math.abs(dX) + Math.abs(dY) + Math.abs(dZ) != 1)
						continue;
					
					int xx = x + dX;
					int yy = y + dY;
					int zz = z + dZ;
					
					if(within(xx, yy, zz))
					{
						if(!getBlock(xx, yy, zz).isOpaque())
						{
							nonOpaqueFound = true;
							break;
						}
					}
					else
					{
						nonOpaqueFound = true;
						break;
					}
				}
				if(nonOpaqueFound)
					break;
			}
			if(nonOpaqueFound)
				break;
		}
		
		if(nonOpaqueFound)
		{
			Model model = getBlock(x, y, z).getModel();
			if(model != null)
			{
				ArrayListF modelLocations = modelsList.get(model);
				if(modelLocations == null)
				{
					modelLocations = new ArrayListF(30000);
					modelsList.put(model, modelLocations);
				}
				
				modelLocations.add((float) x);
				modelLocations.add((float) y);
				modelLocations.add((float) z);
			}
		}
	}
	
	private boolean within(int x, int y, int z)
	{
		return x >= getBeginningCornerX() && x < getEndingCornerX() &&
				y >= getBeginningCornerY() && y < getEndingCornerY() &&
				z >= getBeginningCornerZ() && z < getEndingCornerZ();
	}
	
	/**
	 * <p>Updates every model in the planet, so call sparingly.</p>
	 */
	public void render()
	{
		for(int z = getBeginningCornerZ(); z < getEndingCornerX(); z++)
		{
			for(int y = getBeginningCornerY(); y < getEndingCornerY(); y++)
			{
				for(int x = getBeginningCornerX(); x < getEndingCornerX(); x++)
				{
					updateModel(x, y, z);
				}
			}
		}
		
		for(Model m : modelsList.keySet())
			modelsList.get(m).shrink(); // Saves memoy
	}
	
	/**
	 * Gets a block at the relative coordinates of the planet
	 * @param x The x coordinate relative to the planet
	 * @param y The y coordinate relative to the planet
	 * @param z The z coordinate relative to the planet
	 * @return The block at a given coordinate relative the planet's center
	 */
	public Block getBlock(int x, int y, int z)
	{
		return Blocks.getBlock(blocks[z - getBeginningCornerZ()][y - getBeginningCornerY()][x - getBeginningCornerX()]);
	}
	
	/**
	 * Sets a block at the given coordinate, relative to the planet and updates the planet's model
	 * @param x The x coordinate, relative to the planet's center
	 * @param y The y coordinate, relative to the planet's center
	 * @param z The z coordinate, relative to the planet's center
	 * @param b The block to set it to
	 */
	public void setBlock(int x, int y, int z, Block b)
	{
		setBlock(x, y, z, b.getId());
	}
	
	/**
	 * Sets a block at the given coordinate, relative to the planet
	 * @param x The x coordinate, relative to the planet's center
	 * @param y The y coordinate, relative to the planet's center
	 * @param z The z coordinate, relative to the planet's center
	 * @param setModel Whether or not the model should be set - this should only be called if you are calling a render() later. (When in doubt, set to true)
	 * @param b The block to set it to
	 */
	public void setBlock(int x, int y, int z, boolean setModel, Block b)
	{
		setBlock(x, y, z, setModel, b.getId());
	}
	
	/**
	 * Sets a block at the given coordinate, relative to the planet and updates the planet's model
	 * @param x The x coordinate, relative to the planet's center
	 * @param y The y coordinate, relative to the planet's center
	 * @param z The z coordinate, relative to the planet's center
	 * @param id The block's id to set it to
	 */
	public void setBlock(int x, int y, int z, short id)
	{
		setBlock(x, y, z, true, id);
	}
	
	/**
	 * Sets a block at the given coordinate, relative to the planet
	 * @param x The x coordinate, relative to the planet's center
	 * @param y The y coordinate, relative to the planet's center
	 * @param z The z coordinate, relative to the planet's center
	 * @param setModel Whether or not the model should be set - this should only be called if you are calling a render() later. (When in doubt, set to true)
	 * @param b The block to set it to
	 */
	public void setBlock(int x, int y, int z, boolean setModel, short id)
	{
		if(blocks == null)
			initBlocks();
		
		int zz = z - getBeginningCornerZ();
		int yy = y - getBeginningCornerY();
		int xx = x - getBeginningCornerX();
		
		if(id != getBlock(x, y, z).getId())
		{
			blocks[zz][yy][xx] = id;
			if(setModel)
				updateModel(x, y, z);
		}
	}
	
	/**
	 * Gets the corner of the planet in the negative x direction
	 * @return The corner of the planet in the negative x direction
	 */
	public int getBeginningCornerX()
	{
		return -getWidth() / 2;
	}
	
	/**
	 * Gets the corner of the planet in the negative y direction
	 * @return The corner of the planet in the negative y direction
	 */
	public int getBeginningCornerY()
	{
		return -getHeight() / 2;
	}
	
	/**
	 * Gets the corner of the planet in the negative z direction
	 * @return The corner of the planet in the negative z direction
	 */
	public int getBeginningCornerZ()
	{
		return -getLength() / 2;
	}
	
	/**
	 * Gets the corner of the planet in the positive x direction
	 * @return The corner of the planet in the positive x direction
	 */
	public int getEndingCornerX()
	{
		return getWidth() / 2;
	}
	
	/**
	 * Gets the corner of the planet in the positive y direction
	 * @return The corner of the planet in the positive y direction
	 */
	public int getEndingCornerY()
	{
		return getHeight() / 2;
	}
	
	/**
	 * Gets the corner of the planet in the positive z direction
	 * @return The corner of the planet in the positive z direction
	 */
	public int getEndingCornerZ()
	{
		return getLength() / 2;
	}
	
	/**
	 * The x of the planet relative to the position of the sector
	 * @return The x of the planet relative to the position of the sector
	 */
	public float getSectorX() { return planetX; }
	
	/**
	 * The y of the planet relative to the position of the sector
	 * @return The y of the planet relative to the position of the sector
	 */
	public float getSectorY() { return planetY; }
	
	/**
	 * The z of the planet relative to the position of the sector
	 * @return The z of the planet relative to the position of the sector
	 */
	public float getSectorZ() { return planetZ; }
	
	/**
	 * Sets the x of the planet relative to the position of the sector
	 */
	public void setPlanetX(float x) { this.planetX = x; }
	
	/**
	 * Sets the y of the planet relative to the position of the sector
	 */
	public void setPlanetY(float y) { this.planetY = y; }
	
	/**
	 * Sets the z of the planet relative to the position of the sector
	 */
	public void setPlanetZ(float z) { this.planetZ = z; }
	
	/**
	 * Gets the array of blocks as an array of shorts that have each block's ID
	 * @return The array of blocks as an array of shorts that have each block's ID
	 */
	public short[][][] getBlocks() { return blocks; }
	
	/**
	 * The width of blocks the planet can hold
	 * @return The width of blocks the planet can hold
	 */
	public int getWidth() { return width; }
	
	/**
	 * The height of blocks the planet can hold
	 * @return The height of blocks the planet can hold
	 */
	public int getHeight() { return height; }
	
	/**
	 * The length of blocks the planet can hold
	 * @return The length of blocks the planet can hold
	 */
	public int getLength() { return length; }
	
	/**
	 * Gets every model the planet has and every position that model is at
	 * @return Every model the planet has and every position that model is at
	 */
	public Map<Model, ArrayListF> getModelsAndPositions()
	{
		if(modelsList.size() == 0)
			render();
		return modelsList;
	}
	
	/**
	 * Gets the absolute center position of the planet
	 * @return The absolute center position of the planet
	 */
	public float getUniverseX()
	{
		return sector.getAbsoluteX() + getSectorX() * Sector.CHUNK_DIMENSIONS;
	}
	
	/**
	 * Gets the absolute center position of the planet
	 * @return The absolute center position of the planet
	 */
	public float getUniverseY()
	{
		return sector.getAbsoluteY() + getSectorY() * Sector.CHUNK_DIMENSIONS;
	}
	
	/**
	 * Gets the absolute center position of the planet
	 * @return The absolute center position of the planet
	 */
	public float getUniverseZ()
	{
		return sector.getAbsoluteZ() + getSectorZ() * Sector.CHUNK_DIMENSIONS;
	}
	
	/**
	 * <p>Sets the sector that the planet is a part of</p>
	 * <p>Make sure to update variables such as the planet's position in the sector!</p>
	 * @param sector The sector to set it to
	 */
	public void setSector(Sector sector)
	{
		this.sector = sector;
	}

	/**
	 * Gets if the planet has been generated
	 * @return If the planet has been generated or not
	 */
	public boolean isGenerated() { return generated; }

	public void setGenerated(boolean b) 
	{ 
		this.generated = b;
	}
}