package me.halfquark.transfix;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonListener implements Listener {
	
	//Really smoll number
	private double e = 1E-10;
	
	//Piston extension
	@EventHandler(priority = EventPriority.HIGHEST)
	public void pistonExtend(BlockPistonExtendEvent event) {
		//Blocks to be considered in translocation
		Set<Block> blockList = new HashSet<Block>();
		blockList.addAll(event.getBlocks());
			//Also add the piston block for tesselation (not included in event.getBlocks())
		blockList.add(event.getBlock());
		//Gives the piston face direction
		BlockFace bf = event.getDirection();
		//Translocation
		translocate(event.getBlock(), blockList, bf.getModX(), bf.getModY(), bf.getModZ());
	}
	
	//Piston retraction
	@EventHandler(priority = EventPriority.HIGHEST)
	public void pistonRetract(BlockPistonRetractEvent event) {
		//Blocks to be considered in translocation
		Set<Block> blockList = new HashSet<Block>();
		blockList.addAll(event.getBlocks());
			//Piston face direction
		BlockFace bf;
			//For some weird reason for sticky pistons (corresponding to the event block
			// being PISTON_MOVING_PIECE) event.getDirection() gives the opposite face
			// so we just flip it again.
		if(event.getBlock().getType().equals(Material.PISTON_MOVING_PIECE))
			bf = event.getDirection().getOppositeFace();
		else
			bf = event.getDirection();
			//Add the piston head block to the block list (not included in event.getBlocks())
		blockList.add(event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ())));
		//Translocation
		translocate(event.getBlock(), blockList, -1*bf.getModX(), -1*bf.getModY(), -1*bf.getModZ());
	}
	
	private void translocate(Block pistonBlock, Set<Block> blockList, int tx, int ty, int tz) {
		World world = pistonBlock.getWorld();
		Chunk chunk = pistonBlock.getLocation().getChunk();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		//Check this chunk and adjacent chunks for TNT
		//	###
		//	#X#		X - Piston chunk
		//	###
		for(int dx = -1; dx <= 1; dx++) {
			for(int dz = -1; dz <= 1; dz++) {
				Chunk adjChunk = world.getChunkAt(chunkX + dx, chunkZ + dz);
				if(!TNTManager.tntMap.containsKey(adjChunk))
					continue;
				//Iterate over the TNT entities in this chunk
				for(TNTPrimed tnt : TNTManager.tntMap.get(adjChunk)) {
					if(!tnt.isValid() || tnt.getFuseTicks() <= 0)
						continue;
					double x = tnt.getLocation().getX();
					double y = tnt.getLocation().getY();
					double z = tnt.getLocation().getZ();
					//Check if the TNT is inside the piston and if so align it on the axis
					// of movement to prevent weird 1.12 behaviour where the tnt would be
					// "translocated" but clip on the block below the piston and not fall
					//Note: TNT entity has a width of 0.98 meters
					if(Math.abs((double) pistonBlock.getX() + 0.5D - tnt.getLocation().getX()) <= 0.01D + e + 0.48D*Math.abs(tx) &&
					   Math.abs((double) pistonBlock.getY() + 0.01D - tnt.getLocation().getY()) <= 0.01D + e + 0.48D*Math.abs(ty) &&
					   Math.abs((double) pistonBlock.getZ() + 0.5D - tnt.getLocation().getZ()) <= 0.01D +  e + 0.48D*Math.abs(tz)) {
						if(tx != 0) {
							x = Math.ceil(x) - 0.5;
						} else if(tz != 0) {
							z = Math.ceil(z) - 0.5;
						} else {
							y = pistonBlock.getY();
							//Fixes weird bug with upwards retract translocation
							if(ty > 0)
								y += 0.1;
						}
						tnt.teleport(new Location(world, x, y, z));
					}
					//Iterate over the block list
					for(Block block : blockList) {
						//Check if the TNT is inside the block on all axis except the movement axis
						// where it can be partially outside as long as at least half of the TNT is
						// inside the block.
						//Note: TNT entity has a width of 0.98 meters
						if(Math.abs((double) block.getX() + 0.5D - tnt.getLocation().getX()) <= 0.01D + e + 0.48D*Math.abs(tx) &&
						   Math.abs((double) block.getY() + 0.01D - tnt.getLocation().getY()) <= 0.01D + e + 0.48D*Math.abs(ty) &&
						   Math.abs((double) block.getZ() + 0.5D - tnt.getLocation().getZ()) <= 0.01D +  e + 0.48D*Math.abs(tz)) {
							//Align and teleport the TNT 1 meter on the axis of movement, the block
							// movement will naturally move the TNT 1 more block over if there is
							// available space.
							//This was the best simple and reliable synchronous solution I found.
							if(tx != 0) {
								if(tx > 0)
									x = Math.ceil(x) + 0.5;
								else
									x = Math.floor(x) - 0.5;
							} else if(tz != 0) {
								if(tz > 0)
									z = Math.ceil(z) + 0.5;
								else
									z = Math.floor(z) - 0.5;
							} else {
								if(ty > 0)
									y = Math.ceil(y + 0.1);
								else
									y = Math.floor(y - 0.1);
							}
							tnt.teleport(new Location(world, x, y, z));
							break;
						}
					}
				}
			}
		}
	}
}
