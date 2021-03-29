package me.halfquark.transfix;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class TransFix extends JavaPlugin{
	
	public static TransFix instance;
	
	@Override
	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		//Creates a TNT Manager per world
		for(World world : Bukkit.getWorlds()) {
			TNTManager tntManager = new TNTManager(world);
	        tntManager.runTaskTimer(this, 0, 1);
		}
		getServer().getPluginManager().registerEvents(new PistonListener(), this);
	}
}
