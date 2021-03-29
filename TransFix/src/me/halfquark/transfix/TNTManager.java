package me.halfquark.transfix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTManager extends BukkitRunnable{
	
	private long lastCheck = 0;
	private long lastClean = 0;
	private int cleanTicks = 0;
	private World world;
	//Stores TNT objects with their location's chunk as the key for fast synchronous queries
	public static HashMap<Chunk, Set<TNTPrimed>> tntMap;
	
	public TNTManager(World w) {
		world = w;
		tntMap = new HashMap<Chunk, Set<TNTPrimed>>();
		cleanTicks = TransFix.instance.getConfig().getInt("cleanTicks");
	}
	
	@Override
	public void run() {
		long ticksElapsed = (System.currentTimeMillis() - lastCheck) / 50;
	    if (ticksElapsed <= 0) {
	        return;
	    }
	    //Cleans invalid TNT from the tntMap at set intervals
	    ticksElapsed = (System.currentTimeMillis() - lastClean) / 50;
	    if(ticksElapsed > cleanTicks) {
	    	cleanTNTMap();
	    	lastClean = System.currentTimeMillis();
	    }
	    //Loops through all TNT entities in world
	    for (TNTPrimed tnt : world.getEntitiesByClass(TNTPrimed.class)) {
	    	Chunk tntChunk = tnt.getLocation().getChunk();
	    	//Checks if this TNT has already been processed
            if(tnt.hasMetadata("TransFixChunk")) {
            	if(tnt.getMetadata("TransFixChunk") != null && !tnt.getMetadata("TransFixChunk").isEmpty()) {
            		Chunk tntMetaChunk = (Chunk) tnt.getMetadata("TransFixChunk").get(0).value();
	            	//Removes TNT from the tntMap if invalid
            		if(!tnt.isValid() || tnt.getFuseTicks() <= 0) {
	            		removeTNT(tnt, tntMetaChunk, true);
	            		continue;
	            	}
            		//Changes TNT key in the tntMap if the TNT has moved
	            	if(!tntMetaChunk.equals(tntChunk)) {
	            		addTNT(tnt);
	            		removeTNT(tnt, tntMetaChunk, false);
	            		tnt.setMetadata("TransFixChunk", new FixedMetadataValue(TransFix.instance, tntChunk));
	            	}
	            	continue;
            	}
            }
            //Marks new TNT entities with custom metadata tag
            tnt.setMetadata("TransFixChunk", new FixedMetadataValue(TransFix.instance, tntChunk));
            //Stores TNT objects in the tntMap
            addTNT(tnt);
	    }
	    lastCheck = System.currentTimeMillis();
	}
	
	//Cleans all invalid or blown up TNT from the tntMap
	private void cleanTNTMap() {
		HashMap<Chunk, Set<TNTPrimed>> map = new HashMap<Chunk, Set<TNTPrimed>>();
		for(Chunk chunk : tntMap.keySet()) {
			Set<TNTPrimed> set = new HashSet<TNTPrimed>(tntMap.get(chunk));
			map.put(chunk, set);
		}
		for(Chunk chunk : map.keySet()) {
			if(!tntMap.containsKey(chunk))
				continue;
			if(tntMap.get(chunk) == null) {
				tntMap.remove(chunk);
				continue;
			}
			for(TNTPrimed tnt : map.get(chunk)) {
				if(!tnt.isValid() || tnt.getFuseTicks() <= 0) {
					if(tnt.hasMetadata("TransFixChunk")) {
		            	if(tnt.getMetadata("TransFixChunk") != null && !tnt.getMetadata("TransFixChunk").isEmpty()) {
		            		removeTNT(tnt, (Chunk) tnt.getMetadata("TransFixChunk").get(0).value(), true);
		            		continue;
		            	}
					}
					removeTNT(tnt);
            	}
			}
		}
	}
	
	//Adds the specified TNT entity to the tntMap
	private void addTNT(TNTPrimed tnt) {
		Chunk tntChunk = tnt.getLocation().getChunk();
		tntMap.putIfAbsent(tntChunk, new HashSet<TNTPrimed>());
        tntMap.get(tntChunk).add(tnt);
	}
	
	//Removes the specified TNT from the tntMap using the specified chunk key.
	//If hardRemove is true it will try to hard-remove the TNT if it fails to
	// find it with the key.
	private void removeTNT(TNTPrimed tnt, Chunk chunk, boolean hardRemove) {
		if(!tntMap.containsKey(chunk))
			return;
		if(tntMap.get(chunk).remove(tnt)) {
			if(tntMap.get(chunk).isEmpty())
				tntMap.remove(chunk);
			return;
		}
		if(hardRemove)
			removeTNT(tnt);
		return;
	}
	
	//Hard-removes the TNT from the tntMap first checking the key corresponding to the
	// current location of the TNT and then, if not successful, iterating over every key
	// in the map
	private void removeTNT(TNTPrimed tnt) {
		Chunk tntChunk = tnt.getLocation().getChunk();
		if(tntMap.containsKey(tntChunk)) {
			if(tntMap.get(tntChunk).remove(tnt)) {
				if(tntMap.get(tntChunk).isEmpty())
					tntMap.remove(tntChunk);
				return;
			}
		}
		for(Chunk chunk : tntMap.keySet()) {
			if(tntMap.get(chunk).remove(tnt)) {
				if(tntMap.get(chunk).isEmpty())
					tntMap.remove(chunk);
				return;
			}
		}
		return;
	}
	
}
