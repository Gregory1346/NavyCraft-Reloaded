package com.maximuspayne.navycraft;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

@SuppressWarnings("deprecation")
public class Utils {

	public static void saveSchem(Player player, String schematicName, String customName, ProtectedRegion region, org.bukkit.World world){
        try {
            File file = new File(NavyCraft.instance.getDataFolder(), "/schematics/" + schematicName + "-" + customName + ".schematic");
            File dir = new File(NavyCraft.instance.getDataFolder(), "/schematics/");
            if (!dir.exists())
                dir.mkdirs();
            for (File f : dir.listFiles()) {
            	if (f.getName().contains(schematicName)) {
            		file = f;
            	}
            }
        World weWorld = new BukkitWorld(world);
        
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
        CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);;
        clipboard.copy(editSession);
        SchematicFormat.MCEDIT.save(clipboard, file);
        } catch (IOException | DataException ex) {
            ex.printStackTrace();
        }
    }
	
	public static String getUUIDfromPlayer(String player) {
		String UUID = NavyCraft.instance.getServer().getOfflinePlayer(player).getUniqueId().toString();
		return UUID;
		}
	public static boolean CheckEnabledWorld(Location loc) {
		if(!NavyCraft.instance.getConfig().getString("EnabledWorlds").equalsIgnoreCase("null")) {
			String[] worlds = NavyCraft.instance.getConfig().getString("EnabledWorlds").split(",");
			for(int i = 0; i < worlds.length; i++) {
				if( loc.getWorld().getName().equalsIgnoreCase(worlds[i]) )
				{
					return true;
				}
					
			}
			return false;
		}
		return true;
	}
	public static boolean CheckBattleWorld(Location loc) {
		if(!NavyCraft.instance.getConfig().getString("BattleWorld").equalsIgnoreCase("null")) {
			String[] worlds = NavyCraft.instance.getConfig().getString("BattleWorld").split(",");
			for(int i = 0; i < worlds.length; i++) {
				if( loc.getWorld().getName().equalsIgnoreCase(worlds[i]) )
				{
					return true;
				}
					
			}
			return false;
		}
		return true;
	}
}
