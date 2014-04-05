package uk.co.mobsoc.mglite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
	public static String spawnWorld=null;
	public static String currentWorld;
	public static Plugin main;
	public void onEnable(){
		main = this;
		
		// Due to internal working of some sort of magic, the world defined in server.properties cannot ever be unloaded.
		spawnWorld = Bukkit.getWorlds().get(0).getName();
		SpectateListener sl = new SpectateListener();
		RespawnListener rl = new RespawnListener();
		Bukkit.getPluginManager().registerEvents(sl, this);
		Bukkit.getPluginManager().registerEvents(rl, this);
	}
	public void onDisable(){
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	if(args.length==0){
    		return false;
    	}else{
    		if(args[0].equalsIgnoreCase("load")){
    			if(args.length!=2){
    				sender.sendMessage("/game load WORLDNAME");
    				return true;
    			}
    			if(sender.hasPermission("games.loadmap")){
    				String s = loadWorld(args[1]);
    				if(s!=null){
    					sender.sendMessage(s);
    				}
    				return true;
    			}else{
    				sender.sendMessage("Need permission to load a game map");
    				return true;
    			}
    		}else if(args[0].equalsIgnoreCase("unload")){
    			if(sender.hasPermission("games.unloadmap")){
    				sender.sendMessage("Attempting to unload world");

    				// Since we cannot guarantee that a world will be unloaded when we ask it to,
    				// keep trying until it succeeds.
    				new WorldUnloader(Plugin.currentWorld);
    				currentWorld=null;
    				return true;
    			}else{
    				sender.sendMessage("Need permission to unload a game map");
    				return true;
    			}
    		}else if(args[0].equalsIgnoreCase("spectate")){
    			if(args.length!=3){
    				sender.sendMessage("/game spectate PlayerName [on/off]");
    				return true;
    			}
    			Player p = Bukkit.getPlayer(args[1]);
    			if(p==null){
    				sender.sendMessage("Unknown player : "+args[1]);
    			}
    			String name = p.getName().toLowerCase();
    			if(args[2].equalsIgnoreCase("on")){
    				if(SpectateListener.spectators.contains(name)){
    					sender.sendMessage(p.getName()+" was already spectating!");
    				}else{
    					p.sendMessage("You are now spectating!");
    					p.setAllowFlight(true);
    					p.setGameMode(GameMode.CREATIVE);
    					SpectateListener.hidePlayer(p);
    					SpectateListener.spectators.add(name);
    				}
    			}else{
    				if(!SpectateListener.spectators.contains(name)){
    					sender.sendMessage(p.getName()+" was not spectating!");
    				}else{
    					p.sendMessage("You are no longer spectating!");
    					p.setAllowFlight(false);
    					p.setGameMode(GameMode.ADVENTURE);
    					SpectateListener.showPlayer(p);
    					SpectateListener.spectators.remove(name);
    					name = spawnWorld;
    					if(currentWorld!=null){
    						name = currentWorld;
    					}
    					Location spawn = Bukkit.getWorld(name).getSpawnLocation();
    					p.teleport(spawn);
    					p.getInventory().clear();
    					p.getInventory().setHelmet(null);
    					p.getInventory().setChestplate(null);
    					p.getInventory().setLeggings(null);
    					p.getInventory().setBoots(null);
    				}
    			}
    			return true;
    		}else if(args[0].equalsIgnoreCase("pvp")){
    			if(args.length!=3){
    				sender.sendMessage("/game pvp [on/off]");
    				return true;
    			}
    			if(sender.hasPermission("games.pvp")){
    				if(args[1].equalsIgnoreCase("off")){
    					if(!Bukkit.getWorld(spawnWorld).getPVP()){
		    				Bukkit.getWorld(spawnWorld).setPVP(false);
	    					sender.sendMessage("Disabling PVP on lobby world");
	    					return true;
    					}else{
    						sender.sendMessage("PVP is already disabled");
    						return true;
    					}
    				}else{
    					if(Bukkit.getWorld(spawnWorld).getPVP()){
	    					Bukkit.getWorld(spawnWorld).setPVP(true);
	    					sender.sendMessage("Enabling PVP on lobby world");
	    					return true;
    					}else{
    						sender.sendMessage("PVP is already enabled");
    						return true;
    					}
    				}
    			}else{
    				sender.sendMessage("Need permission to change pvp setting");
    				return true;
    			}
    		}else if(args[0].equalsIgnoreCase("secret")){
    			sender.sendMessage("The secret is... timj11dude wrote this...");
    		}
    	}
    	return false;
    }
    
    public static String cleanWorldName(String name){
    	return name.replaceAll("/[^a-zA-Z0-9_-]/", "");
    }
    
    public static ArrayList<String> usedNames= new ArrayList<String>();
    public static String getUnusedWorldName(){
    	String random = randomString();
    	while(usedNames.contains(random)){
    		random = randomString();
    	}
    	usedNames.add(random);
    	return random;
    }
    
    public static String randomString(){
    	UUID uuid = UUID.randomUUID();
    	return uuid.toString();
    }
	
    public static String loadWorld(String worldName){
		if(currentWorld != null){ return "Another world is already loaded!"; }
		if(worldName == null){ return "Cannot accept NULL world"; }
		worldName = cleanWorldName(worldName);
		
		//if(spawnWorld.equalsIgnoreCase(worldName)){ return "Cannot reload spawn world"; }
		//if(Bukkit.getWorld(worldName)!=null){ return "World already loaded!"; }
		//if(WorldUnloader.isUnloading(worldName)){ return "World is still unloading from its last use!"; }
		if(WorldUnloader.unloading.size()!=0){
			return "A world is still waiting to unload!";
		}
		String s = "Loading new Game map - '"+worldName+"'";
		//Bukkit.unloadWorld(worldName, false);
		File zip = new File("plugins/MobsGamesLite/"+worldName+".zip");
		if(!zip.exists()){
			return "Couldn't find file 'plugins/MobsGamesLite/"+worldName+".zip'";
		}
		String newFolderName = getUnusedWorldName();
		File dest = new File(newFolderName);
		//delete(dest.getAbsoluteFile());
		for(Player p : Bukkit.getOnlinePlayers()){
			p.sendMessage(s);
		}
		zipUnzip(zip, dest);
		Bukkit.getServer().createWorld(new WorldCreator(newFolderName));
		currentWorld = newFolderName;
		Location newSpawn = Bukkit.getWorld(newFolderName).getSpawnLocation();
		for(Player p : Bukkit.getOnlinePlayers()){
			p.setHealth(20f);
			p.setFoodLevel(20);
			p.teleport(newSpawn);
		}
		return null;
	}
	
	public static void delete(File location){
		if(!location.exists()){ System.out.println("Does not exist : "+location); return; }
		if(location.isDirectory()){
			for(File file : location.listFiles()){
				delete(file);
			}
		}
		if(!location.delete()){
			System.out.println("Failed to delete file : "+location);
			System.out.println("Expect funkyness.");
		}
	}
	
	public static void zipUnzip(File zip, File dest){
		if(dest == null || zip == null){
			return;
		}
		byte[] buffer = new byte[1024];
		try{
			if(!dest.exists()){
				dest.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				String fileName = ze.getName();
				File newFile = new File(dest.getPath()+File.separator+fileName);
				if(ze.isDirectory()){
					newFile.mkdirs();
					System.out.println("making directory : "+newFile.getAbsoluteFile());
					ze = zis.getNextEntry();
					continue;
				}
				System.out.println("unzipping file : "+newFile.getAbsoluteFile());
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while((len = zis.read(buffer)) > 0){
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
