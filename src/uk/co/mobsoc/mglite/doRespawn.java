package uk.co.mobsoc.mglite;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class doRespawn implements Runnable{
	String pN = "";
	
	public doRespawn(Player p){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.main, this);
		pN = p.getName();
	}

	@Override
	public void run() {
		// If it looks stupid but it works, It's still stupid.
        try {
        	Player p = Bukkit.getPlayer(pN);
        	if(p==null){ return; }
            Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
            Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").newInstance();
            Class<?> enumClass = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");
 
            for(Object ob : enumClass.getEnumConstants()){
                if(ob.toString().equals("PERFORM_RESPAWN")){
                    packet = packet.getClass().getConstructor(enumClass).newInstance(ob);
                }
            }
 
            Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
        }
        catch(Throwable t){
            t.printStackTrace();
        }
	}

}
