package com.maximuspayne.navycraft;


import com.maximuspayne.navycraft.NavyCraft;
import com.maximuspayne.navycraft.craft.CraftType;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

/**
 * Permissions support file to interface Nijikokun's Permissions plugin to NavyCraft
*/

@SuppressWarnings("deprecation")
public class PermissionInterface {
	public static NavyCraft plugin;
	//public static PermissionInfo Permissions = null;

	public static void setupPermissions(NavyCraft p) {
		plugin = p;
		PluginManager pm = NavyCraft.instance.getServer().getPluginManager();
		if(pm != null) {
			pm.addPermission(new Permission("navycraft.periscope.use"));
			pm.addPermission(new Permission("navycraft.aa-gun.use"));
			pm.addPermission(new Permission("navycraft.flak-gun.use"));
			pm.addPermission(new Permission("navycraft.periscope.create"));
			pm.addPermission(new Permission("navycraft.aa-gun.create"));
			pm.addPermission(new Permission("navycraft.flak-gun.create"));
			
			for (CraftType type : CraftType.craftTypes) 
			{
				pm.addPermission(new Permission("navycraft." + type.name + ".release"));
				pm.addPermission(new Permission("navycraft." + type.name + ".info"));
				pm.addPermission(new Permission("navycraft." + type.name + ".takeover"));
				pm.addPermission(new Permission("navycraft." + type.name + ".start"));
				pm.addPermission(new Permission("navycraft." + type.name + ".create"));
				pm.addPermission(new Permission("navycraft." + type.name + ".sink"));
				pm.addPermission(new Permission("navycraft." + type.name + ".remove"));
			}
		}
	}
	
	public static void removePermissions(NavyCraft p) {
		plugin = p;
		PluginManager pm = NavyCraft.instance.getServer().getPluginManager();
		if(pm != null) {
			pm.removePermission(new Permission("navycraft.periscope.use"));
			pm.removePermission(new Permission("navycraft.aa-gun.use"));
			pm.removePermission(new Permission("navycraft.flak-gun.use"));
			pm.removePermission(new Permission("navycraft.periscope.create"));
			pm.removePermission(new Permission("navycraft.aa-gun.create"));
			pm.removePermission(new Permission("navycraft.flak-gun.create"));
			
			for (CraftType type : CraftType.craftTypes) 
			{
				pm.removePermission(new Permission("navycraft." + type.name + ".release"));
				pm.removePermission(new Permission("navycraft." + type.name + ".info"));
				pm.removePermission(new Permission("navycraft." + type.name + ".takeover"));
				pm.removePermission(new Permission("navycraft." + type.name + ".start"));
				pm.removePermission(new Permission("navycraft." + type.name + ".create"));
				pm.removePermission(new Permission("navycraft." + type.name + ".sink"));
				pm.removePermission(new Permission("navycraft." + type.name + ".remove"));
			}
		}
	}
	
	public static String getUUIDfromPlayer(String player) {
		String UUID = NavyCraft.instance.getServer().getOfflinePlayer(player).getUniqueId().toString();
		return UUID;
		}
	
	public static boolean CheckPerm(Player player, String command) {		
		command = command.replace(" ", ".");
		NavyCraft.instance.DebugMessage("Checking if " + player.getName() + " can " + command, 3);
		
		
		    if( player.hasPermission(command) || player.isOp() ) 
		    {
		    	NavyCraft.instance.DebugMessage("Player has permissions: " + command, 3);
		    	NavyCraft.instance.DebugMessage("Player isop: " + 
		    			player.isOp(), 3);
		    	return true;
		    } else 
		    {
				player.sendMessage(ChatColor.RED + "You do not have permission to perform " + ChatColor.YELLOW + command);
				return false;
		    }
	}
	
	public static boolean CheckQuietPerm(Player player, String command) {		
		command = command.replace(" ", ".");
		NavyCraft.instance.DebugMessage("Checking if " + player.getName() + " can " + command, 3);
		
		
		    if( player.hasPermission(command) || player.isOp() ) 
		    {
		    	NavyCraft.instance.DebugMessage("Player has permissions: " + command, 3);
		    	NavyCraft.instance.DebugMessage("Player isop: " + 
		    			player.isOp(), 3);
		    	return true;
		    } else 
		    {
				//player.sendMessage("You do not have permission to perform " + command);
				return false;
		    }
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
	
	public static void saveSchem(Player player, String schematicName, ProtectedRegion region, org.bukkit.World world){
        try {
            File file = new File(NavyCraft.instance.getDataFolder(), "/schematics/" + schematicName);
            File dir = new File(NavyCraft.instance.getDataFolder(), "/schematics/");
            if (!dir.exists())
                dir.mkdirs();
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
}