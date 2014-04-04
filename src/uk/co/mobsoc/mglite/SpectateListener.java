package uk.co.mobsoc.mglite;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
/**
 * I will refer to players as one of two types in this class
 * Player - Everyone involved in a game. Will also assume anyone watching without using this plugins spectator mode.
 * Spectator - Every player who has had "/game specatate NAME on" used on them.
 * @author triggerhapp
 *
 */
public class SpectateListener implements Listener{
	public static ArrayList<String> spectators = new ArrayList<String>();
	HashMap<String, String> lastView = new HashMap<String, String>();
	
	/**
	 * Stop spectators interacting with blocks at all.
	 * Also check for left or right click to teleport. 
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerInteractEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
			ArrayList<Player> playersLeft = getNonSpectators();
			if(playersLeft.size()==0){ return; }
			if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction()== Action.LEFT_CLICK_BLOCK){
				int idx = getNextIndex(playerName, -1);
				Player p = playersLeft.get(idx).getPlayer();
				if(p!=null){
					event.getPlayer().teleport(p.getLocation());
				}
			}else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
				int idx = getNextIndex(playerName, 1);

				Player p = playersLeft.get(idx).getPlayer();
				if(p!=null){
					event.getPlayer().teleport(p.getLocation());
				}
			}
		}
	}
	
	/**
	 * Get a list of all players who are not spectators
	 * @return
	 */
	private ArrayList<Player> getNonSpectators(){
		ArrayList<Player> playersLeft = new ArrayList<Player>();
		for(Player p : Bukkit.getOnlinePlayers()){
			if(!spectators.contains(p.getName().toLowerCase())){
				playersLeft.add(p);					
			}
		}
		return playersLeft;
	}
	
	/**
	 * get the index of the last viewed player for this spectator, so we can +1 or -1 from the index to get the next player
	 * @param list
	 * @param var
	 * @return
	 */
	private int getIndex(ArrayList<Player> list, String var){
		for(int i = 0; i < list.size(); i++){
			if(list.get(i).getName().equalsIgnoreCase(var)){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Loop through the list of all players to find the next one this spectator can view.
	 * @param playerName
	 * @param dir
	 * @return
	 */
	private int getNextIndex(String playerName, int dir) {
		if(lastView==null){ System.out.println("Big problems - Let me know"); return 0;}
		if(!lastView.containsKey(playerName)){ System.out.println("First spectate for "+playerName); lastView.put(playerName, ""); return 0 ;}
		String last = lastView.get(playerName);
		ArrayList<Player> playersLeft = getNonSpectators();
		int start = getIndex(playersLeft, last);

		if(dir!=-1 && dir!=1){
			dir = 1;
		}
		Player thisOne = Bukkit.getPlayer(playerName);
		Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = sb.getPlayerTeam(thisOne);
		if(start == -1){ start = 0; }
		int current = start+dir;
		if(current>= playersLeft.size()){ current = 0; }
		if(current<0){ current = playersLeft.size()-1; }
		boolean checkTeam = false;
		if(t!=null){ System.out.println("Forcing "+playerName+" to only view "+t.getName()); checkTeam=true; }
		while(current != start){
			Player p = playersLeft.get(current).getPlayer();
			if((checkTeam && t == sb.getPlayerTeam(p)) || !checkTeam){
				lastView.put(playerName, p.getName());
				return current; 
			}
			current += dir;
			if(current>= playersLeft.size()){ current = 0; }
			if(current<0){ current = playersLeft.size()-1; }

		}
		Player p = playersLeft.get(start).getPlayer();
		lastView.put(playerName, p.getName());

		return start;
	}


	/**
	 * Stop spectators from breaking blocks
	 * @param event
	 */
	@EventHandler
	public void stahp(BlockBreakEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators from placing blocks
	 * @param event
	 */
	@EventHandler
	public void stahp(BlockPlaceEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}		
	}
	/**
	 * Stops spectators from dropping items
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerDropItemEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}		
	}
	/**
	 * Stops spectators from mounting, trading, or otherwise interacting with Mobs
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerInteractEntityEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators from picking up items
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerPickupItemEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators from shearing sheep or mooshrooms. Why would they ever do this?
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerShearEntityEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators from filling up a bucket
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerBucketFillEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators from emptying buckets
	 * @param event
	 */
	@EventHandler
	public void stahp(PlayerBucketEmptyEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops spectators giving or recieving damage
	 * @param event
	 */
	@EventHandler
	public void stahp(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player){
			String playerName = ((Player)event.getDamager()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
		if(event.getEntity() instanceof Player){
			String playerName = ((Player)event.getEntity()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators taking non-entity based damage
	 * @param event
	 */
	@EventHandler
	public void stahp(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			String playerName = ((Player)event.getEntity()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators breaking vehicles
	 * @param event
	 */
	@EventHandler
	public void stahp(VehicleDamageEvent event){
		if(event.getAttacker() instanceof Player){
			String playerName = ((Player) event.getAttacker()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators from getting into vehicles
	 * @param event
	 */
	@EventHandler
	public void stahp(VehicleEnterEvent event){
		if(event.getEntered() instanceof Player){
			String playerName = ((Player) event.getEntered()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators from breaking paintings (Why was this depricated?)
	 * @param event
	 */
	@EventHandler
	public void stahp(PaintingBreakByEntityEvent event){
		if(event.getRemover() instanceof Player){
			String playerName = ((Player) event.getRemover()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators from placing paintings
	 * @param event
	 */
	@EventHandler
	public void stahp(PaintingPlaceEvent event){
		String playerName = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(playerName)){
			event.setCancelled(true);
		}
	}
	/**
	 * Stops Mobs from targeting spectators
	 * @param event
	 */
	@EventHandler
	public void stahp(EntityTargetEvent event){
		if(event.getTarget() instanceof Player){
			String playerName = ((Player) event.getTarget()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}
	/**
	 * Stops spectators from launching arrows, snowballs, potions etc
	 * @param event
	 */
	@EventHandler
	public void stahp(ProjectileLaunchEvent event){
		if(event.getEntity() instanceof Player){
			String playerName = ((Player) event.getEntity()).getName().toLowerCase();
			if(spectators.contains(playerName)){
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Removes spectators from list of spectators on log out
	 * @param event
	 */
	@EventHandler
	public void logout(PlayerQuitEvent event){
		String name = event.getPlayer().getName().toLowerCase();
		if(spectators.contains(name)){
			spectators.remove(name);
		}
	}
	
	/**
	 * Hides spectators from new players on login
	 * @param event
	 */
	@EventHandler
	public void hideOthers(PlayerJoinEvent event){
		Player p = event.getPlayer();
		for(String s : spectators){
			p.hidePlayer(Bukkit.getPlayer(s));
		}		
	}

	/**
	 * Show ex-spectator again
	 * @param p
	 */
	public static void showPlayer(Player p) {
		for(Player other : Bukkit.getOnlinePlayers()){
			other.showPlayer(p);
		}
	}

	/**
	 * Hide new spectators from everyone online at the time.
	 * @param p
	 */
	public static void hidePlayer(Player p) {
		for(Player other : Bukkit.getOnlinePlayers()){
			other.hidePlayer(p);
		}		
	}

}
