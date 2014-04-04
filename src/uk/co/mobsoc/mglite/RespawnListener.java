package uk.co.mobsoc.mglite;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener{
	@EventHandler
	public void correctRespawn(PlayerRespawnEvent event){
		// We were getting a bug where we would respawn in the lobby world with the co-ordinates of the spawn in the game world. This sorted it out pretty quick.
		if(Plugin.currentWorld!=null){
			World w = Bukkit.getWorld(Plugin.currentWorld);
			if(w!=null){
				Location l = event.getRespawnLocation().clone();
				l.setWorld(w);
				event.setRespawnLocation(l);				
			}
		}
		
	}
	
	/**
	 * Automatically respawn on death
	 * @param event
	 */
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		new doRespawn(event.getEntity());
		event.getEntity().leaveVehicle();
	}
	
	/**
	 * Move new log ins to the lobby spawn. We were getting problems with people logging out 
	 * mid-game and returning to a bit of the map that was no longer able to escape
	 * @param event
	 */
	@EventHandler
	public void onLogin(PlayerJoinEvent event){
		World spawn = Bukkit.getWorld(Plugin.spawnWorld);
		event.getPlayer().teleport(spawn.getSpawnLocation());
	}
}
