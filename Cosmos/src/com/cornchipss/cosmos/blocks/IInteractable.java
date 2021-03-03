package com.cornchipss.cosmos.blocks;

import com.cornchipss.cosmos.Player;
import com.cornchipss.cosmos.structures.Structure;

/**
 * If a block is interactable by the player
 */
public interface IInteractable
{
	/**
	 * Called whenever a player interacts with the block
	 * @param s The structure this is a part of
	 * @param p The player that interacted with this
	 */
	public void onInteract(Structure s, Player p);
}
