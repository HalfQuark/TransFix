package me.halfquark.transfix;

import org.bukkit.plugin.java.JavaPlugin;

public class TransFix extends JavaPlugin{
	
	public static TransFix instance;
	
	@Override
	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		getServer().getPluginManager().registerEvents(new PistonListener(), this);
	}
}
