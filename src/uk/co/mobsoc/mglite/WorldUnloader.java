package uk.co.mobsoc.mglite;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldUnloader implements Runnable{
	// Static list of worlds to unload.
	public static ArrayList<WorldUnloader> unloading = new ArrayList<WorldUnloader>();
	
	/**
	 * Check if a world name corresponds to a world that is unloading 
	 * @param s
	 * @return
	 */
	public static boolean isUnloading(String s){
		for(WorldUnloader wu : unloading){
			if(wu.world.equalsIgnoreCase(s)){
				return true;
			}
		}
		return false;
	}
	
	// How many seconds waited to unload?
	int count = 0;
	// Name of world to unload
	String world="";
	
	public WorldUnloader(String name){
		unloading.add(this);
		world = name;
		setTimer();
	}

	/**
	 * Sets this to re-run every 1 second until it succeeds.
	 */
	private void setTimer() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.main, this, 20);
	}

	@Override
	public void run() {
		if(world==null || world.equals("")){ unloading.remove(this); return; }
		Location spawn = Bukkit.getWorld(Plugin.spawnWorld).getSpawnLocation();
		for(Player p: Bukkit.getWorld(world).getPlayers()){
			p.setHealth(20f);
			p.setFoodLevel(20);
			p.teleport(spawn);
		}
		boolean b = Bukkit.unloadWorld(world, false);
		if(b==false){
			String s = "", sep = "";
			for(Player p : Bukkit.getWorld(world).getPlayers()){
				s = s + sep + p.getName();
				sep = ", ";
			}
			System.out.println("Could not unload '"+world+"' - retry in 1s");
			System.out.println("Players still on death screen : "+s);
		}else{
			System.out.println("Unloading '"+world+"' complete");
			File dest = new File(world).getAbsoluteFile();
			Plugin.delete(dest);
			unloading.remove(this);
			return;
		}
		count ++;
		//world=null;
		setTimer();
	}

}
