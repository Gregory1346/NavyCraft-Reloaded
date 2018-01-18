package com.maximuspayne.navycraft;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.maximuspayne.aimcannon.AimCannonPlayerListener;
import com.maximuspayne.navycraft.plugins.PermissionInterface;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;

import ru.tehkode.permissions.bukkit.PermissionsEx;

@SuppressWarnings({ "deprecation", "unused" })
public class NavyCraft_BlockListener implements Listener {
	public static Craft updatedCraft = null;
	private static NavyCraft plugin;
	public static PermissionsEx pex;
	public static WorldEditPlugin wep;
	public static WorldGuardPlugin wgp;
	public static int lastSpawn = -1;

	public NavyCraft_BlockListener(NavyCraft p) {
		plugin = p;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		Craft theCraft = Craft.getPlayerCraft(event.getPlayer());
		// System.out.println("Updated craft is " + updatedCraft.name + " of type " + updatedCraft.type.name);

		if (theCraft != null) {
			theCraft.addBlock(event.getBlock(), false);
		} else {
			theCraft = Craft.getCraft(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
			if (theCraft != null) {
				theCraft.addBlock(event.getBlock(), false);
			}
		}

	}

	public static void ClickedASign(Player player, Block block, boolean leftClick) {
		// String world = block.getWorld().getName();
		Craft playerCraft = Craft.getPlayerCraft(player);

		Sign sign = (Sign) block.getState();

		if ((sign.getLine(0) == null) || sign.getLine(0).trim().equals("")) { return; }

		String craftTypeName = sign.getLine(0).trim().toLowerCase();

		// remove colors
		craftTypeName = craftTypeName.replaceAll(ChatColor.BLUE.toString(), "");
		int lotType = 0; /// 1=DD, 2=SUB1, 3=SUB2, 4=CL, 5=CA, 6=hangar1, 7=hangar2

		// remove brackets
		if (craftTypeName.startsWith("[")) {
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);
		}

		if (craftTypeName.equalsIgnoreCase("*select*") && (block.getRelative(BlockFace.DOWN, 1).getTypeId() == 22)) {
			BlockFace bf;
			bf = null;
			// bf2 = null;
			switch (block.getData()) {
				case (byte) 0x8:// n
					bf = BlockFace.SOUTH;
					// bf2 = BlockFace.NORTH;
					break;
				case (byte) 0x0:// s
					bf = BlockFace.NORTH;
					// bf2 = BlockFace.SOUTH;
					break;
				case (byte) 0x4:// w
					bf = BlockFace.EAST;
					// bf2 = BlockFace.WEST;
					break;
				case (byte) 0xC:// e
					bf = BlockFace.WEST;
					// bf2 = BlockFace.EAST;
					break;
				default:
					break;
			}

			if (bf == null) {
				player.sendMessage("Sign error...check direction?");
				return;
			}

			if (block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getTypeId() == 68) {
				String spawnName = sign.getLine(3).trim().toLowerCase();
				Sign sign2 = (Sign) block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getState();
				String restrictedName = sign2.getLine(0).trim().toLowerCase();
				String rankStr = sign2.getLine(1).trim().toLowerCase();
				String idStr = sign2.getLine(2).trim().toLowerCase();
				String lotStr = sign2.getLine(3).trim().toLowerCase();
				spawnName = spawnName.replaceAll(ChatColor.BLUE.toString(), "");
				restrictedName = restrictedName.replaceAll(ChatColor.BLUE.toString(), "");
				rankStr = rankStr.replaceAll(ChatColor.BLUE.toString(), "");
				idStr = idStr.replaceAll(ChatColor.BLUE.toString(), "");
				lotStr = lotStr.replaceAll(ChatColor.BLUE.toString(), "");

				if (spawnName.isEmpty()) {
					player.sendMessage("Sign error...no type");
					return;
				}

				int rankReq = -1;
				try {
					rankReq = Integer.parseInt(rankStr);
				} catch (NumberFormatException nfe) {
					player.sendMessage("Sign error...invaild rank number");
					return;
				}

				if ((rankReq < 1) || (rankReq > 10)) {
					player.sendMessage("Sign error...invalid rank requirement");
					return;
				}

				if (lotStr.equalsIgnoreCase("DD") || lotStr.equalsIgnoreCase("SHIP1")) {
					lotType = 1;
				} else if (lotStr.equalsIgnoreCase("SUB1") || lotStr.equalsIgnoreCase("SHIP2")) {
					lotType = 2;
				} else if (lotStr.equalsIgnoreCase("SUB2") || lotStr.equalsIgnoreCase("SHIP3")) {
					lotType = 3;
				} else if (lotStr.equalsIgnoreCase("CL") || lotStr.equalsIgnoreCase("SHIP4")) {
					lotType = 4;
				} else if (lotStr.equalsIgnoreCase("CA") || lotStr.equalsIgnoreCase("SHIP5")) {
					lotType = 5;
				} else if (lotStr.equalsIgnoreCase("HANGAR1")) {
					lotType = 6;
				} else if (lotStr.equalsIgnoreCase("HANGAR2")) {
					lotType = 7;
				} else if (lotStr.equalsIgnoreCase("TANK1")) {
					lotType = 8;
				} else {
					player.sendMessage("Sign error...lot type");
					return;
				}

				String ownerName = sign.getLine(1) + sign.getLine(2);

				if (!restrictedName.isEmpty() && !restrictedName.equalsIgnoreCase("Public") && !restrictedName.equalsIgnoreCase(player.getName()) && !ownerName.equalsIgnoreCase(player.getName()) && !player.isOp() && !PermissionInterface.CheckQuietPerm(player, "NavyCraft.select")) {

					int tpId = -1;
					try {
						tpId = Integer.parseInt(idStr);
					} catch (NumberFormatException e) {
						player.sendMessage("Invalid plot id");
						return;
					}

					if (tpId > -1) {
						NavyCraft_BlockListener.loadShipyard();

						Sign foundSign = null;
						foundSign = NavyCraft_BlockListener.findSign(ownerName, tpId);
						if ((foundSign != null) && foundSign.getLocation().equals(sign.getLocation())) {
							wgp = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
							if (wgp != null) {
								RegionManager regionManager = wgp.getRegionManager(plugin.getServer().getWorld("shipyard"));
								String regionName = "--" + ownerName + "-" + tpId;

								if ((regionManager.getRegion(regionName) != null) && !regionManager.getRegion(regionName).getMembers().contains(player.getName())) {
									player.sendMessage("You are not allowed to select this plot.");
									return;
								}
							}
						} else {
							player.sendMessage("You are not allowed to select this plot.");
							return;
						}
					} else {
						player.sendMessage("Invalid Plot ID");
						return;
					}
				}

				wep = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
				if (wep == null) {
					player.sendMessage("WorldEdit error");
					return;
				}

				EditSession es = wep.createEditSession(player);

				Location loc;
				int sizeX, sizeY, sizeZ, originX, originY, originZ, offsetX, offsetY, offsetZ;
				if (lotType == 1) {
					loc = block.getRelative(bf, 28).getLocation();
					sizeX = 13;
					sizeY = 28;
					sizeZ = 28;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -29;
				} else if (lotType == 2) {
					loc = block.getRelative(bf, 43).getLocation();
					sizeX = 9;
					sizeY = 28;
					sizeZ = 43;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -44;
				} else if (lotType == 3) {
					loc = block.getRelative(bf, 70).getLocation();
					sizeX = 11;
					sizeY = 28;
					sizeZ = 70;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -71;
				} else if (lotType == 4) {
					loc = block.getRelative(bf, 55).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 55;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -56;
				} else if (lotType == 5) {
					loc = block.getRelative(bf, 98).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 98;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -99;
				} else if (lotType == 6) {
					loc = block.getRelative(bf, 17).getLocation();
					sizeX = 17;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
					offsetX = -17;
					offsetY = 0;
					offsetZ = -20;
				} else if (lotType == 7) {
					loc = block.getRelative(bf, 25).getLocation();
					sizeX = 25;
					sizeY = 7;
					sizeZ = 32;
					originX = 0;
					originY = -1;
					originZ = -31;
					offsetX = -25;
					offsetY = 0;
					offsetZ = -33;
				} else if (lotType == 8) {
					loc = block.getRelative(bf, 12).getLocation();
					sizeX = 12;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
					offsetX = -12;
					offsetY = 0;
					offsetZ = -20;
				} else

				{
					player.sendMessage("Sign error...invalid lot");
					return;
				}

				CuboidRegion region = new CuboidRegion(new Vector(loc.getBlockX() + originX, loc.getBlockY() + originY, loc.getBlockZ() + originZ), new Vector((loc.getBlockX() + originX + sizeX) - 1, (loc.getBlockY() + originY + sizeY) - 1, (loc.getBlockZ() + originZ + sizeZ) - 1));

				BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
				try {

					if ((lotType >= 6) && (lotType <= 8)) {
						clipboard.setOrigin(new Vector(block.getX() + 1, block.getY(), (block.getZ() - sizeZ) + 1));
					} else {
						clipboard.setOrigin(new Vector(loc.getX(), loc.getY(), loc.getZ()));
					}

					ForwardExtentCopy copy = new ForwardExtentCopy(es, region, clipboard, region.getMinimumPoint());
					Operations.completeLegacy(copy);
					wep.getSession(player).setClipboard(new ClipboardHolder(clipboard, es.getWorld().getWorldData()));
					Craft.playerClipboards.put(player, wep.getSession(player).getClipboard());

				} catch (MaxChangedBlocksException e) {
					e.printStackTrace();
				} catch (EmptyClipboardException e) {
					e.printStackTrace();
				}
				Craft.playerClipboardsRank.put(player, rankReq);
				Craft.playerClipboardsType.put(player, spawnName);
				Craft.playerClipboardsLot.put(player, lotStr);
				player.sendMessage(ChatColor.YELLOW + "Selected vehicle : " + ChatColor.WHITE + spawnName.toUpperCase());

			} else {
				player.sendMessage("Sign error...check second sign?");
				return;
			}

		} else if (craftTypeName.equalsIgnoreCase("*claim*") && (block.getRelative(BlockFace.DOWN, 1).getTypeId() == 22)) {
			BlockFace bf;
			bf = null;
			// bf2 = null;
			switch (block.getData()) {
				case (byte) 0x8:// n
					bf = BlockFace.SOUTH;
					// bf2 = BlockFace.NORTH;
					break;
				case (byte) 0x0:// s
					bf = BlockFace.NORTH;
					// bf2 = BlockFace.SOUTH;
					break;
				case (byte) 0x4:// w
					bf = BlockFace.EAST;
					// bf2 = BlockFace.WEST;
					break;
				case (byte) 0xC:// e
					bf = BlockFace.WEST;
					// bf2 = BlockFace.EAST;
					break;
				default:
					break;
			}

			if (bf == null) {
				player.sendMessage("Sign error...check direction?");
				return;
			}

			if (block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getTypeId() == 68) {
				Sign sign2 = (Sign) block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getState();
				String lotStr = sign2.getLine(3).trim().toLowerCase();
				lotStr = lotStr.replaceAll(ChatColor.BLUE.toString(), "");

				if (lotStr.equalsIgnoreCase("DD") || lotStr.equalsIgnoreCase("SHIP1")) {
					lotStr = "SHIP1";
					lotType = 1;
				} else if (lotStr.equalsIgnoreCase("SUB1") || lotStr.equalsIgnoreCase("SHIP2")) {
					lotStr = "SHIP2";
					lotType = 2;
				} else if (lotStr.equalsIgnoreCase("SUB2") || lotStr.equalsIgnoreCase("SHIP3")) {
					lotStr = "SHIP3";
					lotType = 3;
				} else if (lotStr.equalsIgnoreCase("CL") || lotStr.equalsIgnoreCase("SHIP4")) {
					lotStr = "SHIP4";
					lotType = 4;
				} else if (lotStr.equalsIgnoreCase("CA") || lotStr.equalsIgnoreCase("SHIP5")) {
					lotStr = "SHIP5";
					lotType = 5;
				} else if (lotStr.equalsIgnoreCase("HANGAR1")) {
					lotType = 6;
				} else if (lotStr.equalsIgnoreCase("HANGAR2")) {
					lotType = 7;
				} else if (lotStr.equalsIgnoreCase("TANK1")) {
					lotType = 8;
				} else {
					player.sendMessage("Sign error...lot type");
					return;
				}

				loadShipyard();
				loadRewards(player.getName());

				Location loc;
				int sizeX, sizeY, sizeZ, originX, originY, originZ, offsetX, offsetY, offsetZ;
				if (lotType == 1) {
					loc = block.getRelative(bf, 28).getLocation();
					sizeX = 13;
					sizeY = 28;
					sizeZ = 28;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -29;

					int numDDs = 0;
					int numRewDDs = 1;
					if (NavyCraft.playerDDSigns.containsKey(player.getName())) {
						numDDs = NavyCraft.playerDDSigns.get(player.getName()).size();
					}
					if (NavyCraft.playerDDRewards.containsKey(player.getName())) {
						numRewDDs = NavyCraft.playerDDRewards.get(player.getName());
					}
					if (numDDs >= numRewDDs) {
						player.sendMessage("You have no SHIP1 reward plots available.");
						return;
					}

				} else if (lotType == 2) {
					loc = block.getRelative(bf, 43).getLocation();
					sizeX = 9;
					sizeY = 28;
					sizeZ = 43;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -44;

					int numSUB1s = 0;
					int numRewSUB1s = 0;
					if (NavyCraft.playerSUB1Signs.containsKey(player.getName())) {
						numSUB1s = NavyCraft.playerSUB1Signs.get(player.getName()).size();
					}
					if (NavyCraft.playerSUB1Rewards.containsKey(player.getName())) {
						numRewSUB1s = NavyCraft.playerSUB1Rewards.get(player.getName());
					}
					if (numSUB1s >= numRewSUB1s) {
						player.sendMessage("You have no SHIP2 reward plots available.");
						return;
					}
				} else if (lotType == 3) {
					loc = block.getRelative(bf, 70).getLocation();
					sizeX = 11;
					sizeY = 28;
					sizeZ = 70;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -71;

					int numSUB2s = 0;
					int numRewSUB2s = 0;
					if (NavyCraft.playerSUB2Signs.containsKey(player.getName())) {
						numSUB2s = NavyCraft.playerSUB2Signs.get(player.getName()).size();
					}
					if (NavyCraft.playerSUB2Rewards.containsKey(player.getName())) {
						numRewSUB2s = NavyCraft.playerSUB2Rewards.get(player.getName());
					}
					if (numSUB2s >= numRewSUB2s) {
						player.sendMessage("You have no SHIP3 reward plots available.");
						return;
					}
				} else if (lotType == 4) {
					loc = block.getRelative(bf, 55).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 55;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -56;

					int numCLs = 0;
					int numRewCLs = 0;
					if (NavyCraft.playerCLSigns.containsKey(player.getName())) {
						numCLs = NavyCraft.playerCLSigns.get(player.getName()).size();
					}
					if (NavyCraft.playerCLRewards.containsKey(player.getName())) {
						numRewCLs = NavyCraft.playerCLRewards.get(player.getName());
					}
					if (numCLs >= numRewCLs) {
						player.sendMessage("You have no SHIP4 reward plots available.");
						return;
					}
				} else if (lotType == 5) {
					loc = block.getRelative(bf, 98).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 98;
					originX = 0;
					originY = -8;
					originZ = 0;
					offsetX = 0;
					offsetY = -7;
					offsetZ = -99;

					int numCAs = 0;
					int numRewCAs = 0;
					if (NavyCraft.playerCASigns.containsKey(player.getName())) {
						numCAs = NavyCraft.playerCASigns.get(player.getName()).size();
					}
					if (NavyCraft.playerCARewards.containsKey(player.getName())) {
						numRewCAs = NavyCraft.playerCARewards.get(player.getName());
					}
					if (numCAs >= numRewCAs) {
						player.sendMessage("You have no SHIP5 reward plots available.");
						return;
					}
				} else if (lotType == 6) {
					loc = block.getRelative(bf, 17).getLocation();
					sizeX = 17;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
					offsetX = -17;
					offsetY = 0;
					offsetZ = -20;

					int numH1s = 0;
					int numRewH1s = 0;
					if (NavyCraft.playerHANGAR1Signs.containsKey(player.getName())) {
						numH1s = NavyCraft.playerHANGAR1Signs.get(player.getName()).size();
					}
					if (NavyCraft.playerHANGAR1Rewards.containsKey(player.getName())) {
						numRewH1s = NavyCraft.playerHANGAR1Rewards.get(player.getName());
					}
					if (numH1s >= numRewH1s) {
						player.sendMessage("You have no HANGAR1 reward plots available.");
						return;
					}
				} else if (lotType == 7) {
					loc = block.getRelative(bf, 25).getLocation();
					sizeX = 25;
					sizeY = 7;
					sizeZ = 32;
					originX = 0;
					originY = -1;
					originZ = -31;
					offsetX = -25;
					offsetY = 0;
					offsetZ = -33;

					int numH2s = 0;
					int numRewH2s = 0;
					if (NavyCraft.playerHANGAR2Signs.containsKey(player.getName())) {
						numH2s = NavyCraft.playerHANGAR2Signs.get(player.getName()).size();
					}
					if (NavyCraft.playerHANGAR2Rewards.containsKey(player.getName())) {
						numRewH2s = NavyCraft.playerHANGAR2Rewards.get(player.getName());
					}
					if (numH2s >= numRewH2s) {
						player.sendMessage("You have no HANGAR2 reward plots available.");
						return;
					}
				} else if (lotType == 8) {
					loc = block.getRelative(bf, 12).getLocation();
					sizeX = 12;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
					offsetX = -12;
					offsetY = 0;
					offsetZ = -20;

					int numT1s = 0;
					int numRewT1s = 0;
					if (NavyCraft.playerTANK1Signs.containsKey(player.getName())) {
						numT1s = NavyCraft.playerTANK1Signs.get(player.getName()).size();
					}
					if (NavyCraft.playerTANK1Rewards.containsKey(player.getName())) {
						numRewT1s = NavyCraft.playerTANK1Rewards.get(player.getName());
					}
					if (numT1s >= numRewT1s) {
						player.sendMessage("You have no TANK1 reward plots available.");
						return;
					}
				} else

				{
					player.sendMessage("Sign error...invalid lot");
					return;
				}
				originX = loc.getBlockX() + originX;
				originY = loc.getBlockY() + originY;
				originZ = loc.getBlockZ() + originZ;

				wgp = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				if (wgp != null) {
					RegionManager regionManager = wgp.getRegionManager(loc.getWorld());

					// ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

					String regionName = "--" + player.getName() + "-" + (maxId(player) + 1);

					regionManager.addRegion(new com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion(regionName, new com.sk89q.worldedit.BlockVector(originX, originY, originZ), new com.sk89q.worldedit.BlockVector((originX + sizeX) - 1, (originY + sizeY) - 1, (originZ + sizeZ) - 1)));
					DefaultDomain owners = new DefaultDomain();
					com.sk89q.worldguard.LocalPlayer lp = wgp.wrapPlayer(player);
					owners.addPlayer(lp);
					regionManager.getRegion(regionName).setOwners(owners);

					try {
						regionManager.save();
					} catch (StorageException e) {
						e.printStackTrace();
					}

					sign.setLine(0, "*Select*");
					if (player.getName().length() > 15) {
						sign.setLine(1, player.getName().substring(0, 16));
						sign.setLine(2, player.getName().substring(15, player.getName().length()));
					} else {
						sign.setLine(1, player.getName());
					}

					sign.setLine(3, "custom");
					sign.update();

					sign2.setLine(0, "Private");
					sign2.setLine(1, "1");
					sign2.setLine(2, "" + (maxId(player) + 1));
					sign2.setLine(3, lotStr);
					sign2.update();

					player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + lotStr.toUpperCase() + "-Plot Claimed!");

				} else {
					player.sendMessage("World Guard error");
				}

			} else {
				player.sendMessage("Sign error...check second sign?");
				return;
			}
		} else if (craftTypeName.equalsIgnoreCase("*recall*") && (block.getRelative(BlockFace.DOWN, 1).getTypeId() == 22)) {
			int xCord = 0;
			int yCord = 0;
			int zCord = 0;
			String xStr = sign.getLine(1).trim().toLowerCase();
			String yStr = sign.getLine(2).trim().toLowerCase();
			String zStr = sign.getLine(3).trim().toLowerCase();
			xStr = xStr.replaceAll(ChatColor.BLUE.toString(), "");
			yStr = yStr.replaceAll(ChatColor.BLUE.toString(), "");
			zStr = zStr.replaceAll(ChatColor.BLUE.toString(), "");
			try {
				xCord = Integer.parseInt(xStr);
				yCord = Integer.parseInt(yStr);
				zCord = Integer.parseInt(zStr);
			} catch (NumberFormatException nfe) {
				player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invalid Coordinates");
				return;
			}
			player.teleport(new Location(player.getWorld(), xCord, yCord, zCord));

		} else if (craftTypeName.equalsIgnoreCase("*spawn*") && (block.getRelative(BlockFace.DOWN, 1).getTypeId() == 22)) {
			int rotate = -1;
			BlockFace bf, bf2;
			bf = null;
			bf2 = null;
			switch (block.getData()) {
				case (byte) 0x8:// n
					rotate = 180;
					bf = BlockFace.SOUTH;
					bf2 = BlockFace.WEST;
					break;
				case (byte) 0x0:// s
					rotate = 0;
					bf = BlockFace.NORTH;
					bf2 = BlockFace.EAST;
					break;
				case (byte) 0x4:// w
					rotate = 90;
					bf = BlockFace.EAST;
					bf2 = BlockFace.SOUTH;
					break;
				case (byte) 0xC:// e
					rotate = 270;
					bf = BlockFace.WEST;
					bf2 = BlockFace.NORTH;
					break;
				default:
					break;
			}

			if (rotate == -1) {
				player.sendMessage(ChatColor.DARK_RED + "Sign Error: Check Direction?");
				return;
			}

			if (!Craft.playerClipboards.containsKey(player)) {
				player.sendMessage(ChatColor.YELLOW + "Go to the Shipyard and select a vehicle first.");
				return;
			}

			if (!checkSpawnerClear(player, block, bf, bf2)) {
				player.sendMessage(ChatColor.RED + "Vehicle in the way.");
				return;
			}

			boolean isAutoSpawn = false;
			boolean isMerchantSpawn = false;
			String freeString = sign.getLine(2).trim().toLowerCase();
			freeString = freeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (freeString.equalsIgnoreCase("merchant")) {
				isMerchantSpawn = true;
			}

			String typeString = sign.getLine(1).trim().toLowerCase();
			typeString = typeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!typeString.isEmpty() && !typeString.equalsIgnoreCase(Craft.playerClipboardsType.get(player)) && !typeString.equalsIgnoreCase(Craft.playerClipboardsLot.get(player))) {
				player.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.RED + ", you cannot spawn this type of vehicle here.");
				return;
			}

			int freeSpawnRankLimit = 0;
			String freeSpawnRankStr = sign.getLine(3).trim().toLowerCase();
			freeSpawnRankStr = freeSpawnRankStr.replaceAll(ChatColor.BLUE.toString(), "");
			if (!freeSpawnRankStr.isEmpty()) {
				try {
					freeSpawnRankLimit = Integer.parseInt(freeSpawnRankStr);
				} catch (NumberFormatException nfe) {
					player.sendMessage("Sign error...invaild rank limit");
					return;
				}
			}

			if (freeSpawnRankLimit < 0) {
				player.sendMessage("Sign error...invaild rank limit");
				return;
			}

			int playerRank = 1;
			String worldName = player.getWorld().getName();
			for(String s:PermissionsEx.getUser(player).getPermissions(worldName)) {
				if( s.contains("navycraft") ) {
					if( s.contains("rank") ) {
						String[] split = s.split("\\.");
						try {
							playerRank = Integer.parseInt(split[2]);	
						} catch (Exception ex) {
							System.out.println("Invalid perm-" + s);
						}
			}

			if ((playerRank < Craft.playerClipboardsRank.get(player)) && (freeSpawnRankLimit < Craft.playerClipboardsRank.get(player)) && !player.isOp()) {
				player.sendMessage(ChatColor.RED + "You do not have the rank to spawn this vehicle.");
				return;
			}
		}
	}


			Essentials ess;
			ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
			if (ess == null) {
				player.sendMessage("Essentials Economy error");
				return;
			}

			wep = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
			if (wep == null) {
				player.sendMessage("WorldEdit error");
				return;
			}
			EditSession es = wep.createEditSession(player);

			try {
				int oldLimit = es.getBlockChangeLimit();
				es.setBlockChangeLimit(50000);

				ClipboardHolder ch = Craft.playerClipboards.get(player);

				int width = ch.getClipboard().getRegion().getWidth();
				int length = ch.getClipboard().getRegion().getLength();
				int moveForward = 0;
				if (width > length) {
					moveForward = width;
				} else {
					moveForward = length;
				}

				AffineTransform transform = new AffineTransform();
				transform = transform.rotateY(-rotate);
				ch.setTransform(transform);
				Block pasteBlock = block.getRelative(bf, moveForward + 2);
				Vector pasteVector = new Vector(pasteBlock.getLocation().getX(), pasteBlock.getLocation().getY(), pasteBlock.getLocation().getZ());
				Operation operation;
				operation = ch.createPaste(es, ch.getWorldData())
						.to(pasteVector).ignoreAirBlocks(false).build();
				Operations.completeLegacy(operation);

				transform = transform.rotateY(rotate);
				ch.setTransform(transform);
				es.flushQueue();

				player.sendMessage(ChatColor.YELLOW + "Spawned vehicle : " + ChatColor.WHITE + Craft.playerClipboardsType.get(player).toUpperCase());


				es.setBlockChangeLimit(oldLimit);

				int shiftRight = 0;
				int shiftForward = 0;
				int shiftUp = 0;
				int shiftDown = 0;

				if (isAutoSpawn || isMerchantSpawn) {
					if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("DD") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP1")) {
						shiftRight = 12;
						shiftForward = 28;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SUB1") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP2")) {
						shiftRight = 8;
						shiftForward = 43;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SUB2") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP3")) {
						shiftRight = 10;
						shiftForward = 70;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("CL") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP4")) {
						shiftRight = 16;
						shiftForward = 55;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("CA") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP5")) {
						shiftRight = 16;
						shiftForward = 98;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("HANGAR1")) {
						shiftRight = 16;
						shiftForward = 19;
						// shiftRight = 0;
						// shiftForward = 0;
						shiftUp = 7;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("HANGAR2")) {
						shiftRight = 24;
						shiftForward = 32;
						// shiftRight = 0;
						// shiftForward = 0;
						shiftUp = 7;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK1")) {
						shiftRight = 11;
						shiftForward = 19;
						shiftUp = 7;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK2")) {
						shiftRight = 0;
						shiftForward = 0;
						shiftUp = 9;
						shiftDown = 0;
					} else {
						player.sendMessage("Unknown lot type error2!");
					}

					Block rightLimit = block.getRelative(bf2, shiftRight).getRelative(bf, shiftForward).getRelative(BlockFace.UP, shiftUp);
					Block leftLimit = block.getRelative(bf, 1).getRelative(BlockFace.DOWN, shiftDown);
					int rightX, rightY, rightZ;
					int leftX, leftY, leftZ;
					rightX = rightLimit.getX();
					rightY = rightLimit.getY();
					rightZ = rightLimit.getZ();
					leftX = leftLimit.getX();
					leftY = leftLimit.getY();
					leftZ = leftLimit.getZ();
					int startX, endX, startZ, endZ;
					if (rightX < leftX) {
						startX = rightX;
						endX = leftX;
					} else {
						startX = leftX;
						endX = rightX;
					}
					if (rightZ < leftZ) {
						startZ = rightZ;
						endZ = leftZ;
					} else {
						startZ = leftZ;
						endZ = rightZ;
					}

					for (int x = startX; x <= endX; x++) {
						for (int y = leftY; y <= rightY; y++) {
							for (int z = startZ; z <= endZ; z++) {
								if (player.getWorld().getBlockAt(x, y, z).getTypeId() == 68) {
									Block shipSignBlock = player.getWorld().getBlockAt(x, y, z);
									Sign shipSign = (Sign) shipSignBlock.getState();
									String signLine0 = shipSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
									CraftType craftType = CraftType.getCraftType(signLine0);
									if (craftType != null) {
										String name = shipSign.getLine(1);// .replaceAll("§.", "");

										if (name.trim().equals("")) {
											name = null;
										}

										int shipx = shipSignBlock.getX();
										int shipy = shipSignBlock.getY();
										int shipz = shipSignBlock.getZ();

										int direction = shipSignBlock.getData();

										// get the block the sign is attached to
										shipx = shipx + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
										shipz = shipz + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

										float dr = 0;

										switch (shipSignBlock.getData()) {
											case (byte) 0x2:// n
												dr = 180;
												break;
											case (byte) 0x3:// s
												dr = 0;
												break;
											case (byte) 0x4:// w
												dr = 90;
												break;
											case (byte) 0x5:// e
												dr = 270;
												break;
										}
										Craft theCraft = NavyCraft.instance.createCraft(player, craftType, shipx, shipy, shipz, name, dr, shipSignBlock, true);

										CraftMover cm = new CraftMover(theCraft, plugin);
										cm.structureUpdate(null, false);

										if (isAutoSpawn) {
											theCraft.isAutoCraft = true;
											theCraft.speedChange(null, true);
											theCraft.speedChange(null, true);
											theCraft.speedChange(null, true);
											theCraft.speedChange(null, true);

											double randomNum = Math.random();
											String speedStr = "LOW";
											if (randomNum >= .3) {
												theCraft.gearChange(null, true);
												speedStr = "MODERATE";
											}
											if (randomNum >= .7) {
												theCraft.gearChange(null, true);
												speedStr = "HIGH";
											}
											String dirStr = "EAST";
											if (randomNum >= .8) {
												theCraft.rudderChange(null, 1, false);
												dirStr = "SOUTH-EAST";
											} else if (randomNum <= .2) {
												theCraft.rudderChange(null, -1, false);
												dirStr = "NORTH-EAST";
											}

											plugin.getServer().broadcastMessage(ChatColor.YELLOW + "**ENEMY MERCHANT ALERT**");
											plugin.getServer().broadcastMessage(ChatColor.YELLOW + "Enemy Maru Ammo Carrier sighted near west central side of ocean...");
											plugin.getServer().broadcastMessage(ChatColor.YELLOW + "coordinates x=-30 z=-300, headed " + dirStr + " at " + speedStr + " speed...");
											plugin.getServer().broadcastMessage(ChatColor.YELLOW + "player who sinks this vehicle will win a cash reward!");
										} else if (isMerchantSpawn) {
											theCraft.isMerchantCraft = true;

											if (theCraft.redTeam) {
												NavyCraft.redMerchant = true;
												plugin.getServer().broadcastMessage(ChatColor.YELLOW + "**" + ChatColor.RED + "Red Team" + ChatColor.YELLOW + " has spawned a merchant!**");
											} else if (theCraft.blueTeam) {

												NavyCraft.blueMerchant = true;
												plugin.getServer().broadcastMessage(ChatColor.YELLOW + "**" + ChatColor.BLUE + "Blue Team" + ChatColor.YELLOW + " has spawned a merchant!**");

											}
										}
										return;
									}
								}
							}
						}
					}
					player.sendMessage("No ship sign located!");
				}

			} catch (MaxChangedBlocksException e) {
				player.sendMessage("Max changed blocks error");
				return;
			}
		} else if (craftTypeName.equalsIgnoreCase("periscope")) {
			if (!PermissionInterface.CheckPerm(player, "navycraft.basic")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this sign");
				return;
			}
			if (NavyCraft.aaGunnersList.contains(player)) {
				NavyCraft.aaGunnersList.remove(player);
				if (player.getInventory().contains(Material.BLAZE_ROD)) {
					player.getInventory().remove(Material.BLAZE_ROD);
				}
				player.sendMessage(ChatColor.YELLOW + "You get off the AA-Gun.");
			}
			if (NavyCraft.flakGunnersList.contains(player)) {
				NavyCraft.flakGunnersList.remove(player);
				if (player.getInventory().contains(Material.BLAZE_ROD)) {
					player.getInventory().remove(Material.BLAZE_ROD);
				}
				player.sendMessage(ChatColor.YELLOW + "You get off the Flak-Gun.");
			}

			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {


				boolean periscopeFound = false;
				CraftMover cmer = new CraftMover(c, plugin);
				cmer.structureUpdate(null, false);
				for (Periscope p : c.periscopes) {
					if ((block.getLocation().getBlockX() == p.signLoc.getBlockX()) && (block.getLocation().getBlockY() == p.signLoc.getBlockY()) && (block.getLocation().getBlockZ() == p.signLoc.getBlockZ())) {
						periscopeFound = true;
						if (p.user != null) {
							player.sendMessage(ChatColor.RED + "Player already on scope.");
						} else if (p.raised && !p.destroyed && (p.scopeLoc != null)) {
							player.sendMessage(ChatColor.GREEN + "Periscope On!");
							player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
							Location newLoc = new Location(playerCraft.world, p.scopeLoc.getBlockX() + 0.5, p.scopeLoc.getBlockY() + 0.5, p.scopeLoc.getBlockZ() + 0.5);
							newLoc.setYaw(player.getLocation().getYaw());
							player.teleport(newLoc);
							p.user = player;
						} else if (!p.destroyed && (p.scopeLoc != null)) {
							player.sendMessage(ChatColor.YELLOW + "Raise Periscope First.");
						} else {
							player.sendMessage(ChatColor.RED + "Periscope destroyed!");
						}
					}
				}

				if (!periscopeFound) {
					Periscope newPeriscope = new Periscope(block.getLocation(), c.periscopes.size());
					sign.setLine(1, "||" + newPeriscope.periscopeID + "||");
					sign.setLine(2, "|| ||");
					sign.setLine(3, "DOWN");
					sign.update();
					c.periscopes.add(newPeriscope);
					NavyCraft.allPeriscopes.add(newPeriscope);

					CraftMover cm = new CraftMover(c, plugin);
					cm.structureUpdate(null, false);

					if (!newPeriscope.destroyed && (newPeriscope.scopeLoc != null)) {
						Location newLoc = new Location(playerCraft.world, newPeriscope.scopeLoc.getBlockX() + 0.5, newPeriscope.scopeLoc.getBlockY() + 0.5, newPeriscope.scopeLoc.getBlockZ() + 0.5);
						newLoc.setYaw(player.getLocation().getYaw());
						player.teleport(newLoc);
						newPeriscope.user = player;
						player.sendMessage(ChatColor.GREEN + "Periscope Started!");
						player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
					}
				}

			} else {
				player.sendMessage(ChatColor.RED + "Start the sub before using the periscope!");
			}
		} else if (craftTypeName.equalsIgnoreCase("subdrive")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (c.submergedMode) {
					// if( 63 - c.minY == c.keelDepth )
					// {
					player.sendMessage(ChatColor.GOLD + "Starting Diesel Engines (SURFACE MODE)");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
					c.submergedMode = false;
					c.vertPlanes = 0;
					// c.type.maxEngineSpeed = c.type.maxSurfaceSpeed;

					for (int eng : c.engineIDIsOn.keySet()) {
						//int engineType = c.engineIDTypes.get(eng);
						//if ((engineType != 0) && (engineType != 1) && (engineType != 2) && (engineType != 4) && (engineType != 9)) {
						if( c.engineIDSetOn.get(eng) && !c.engineIDIsOn.get(eng) ) {
							c.engineIDIsOn.put(eng, true);
						}
					}


					for (String s : c.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.YELLOW + "Surface the boat!");
						}
					}

					surfaceBellThread(sign.getBlock().getLocation());

				} else {
					player.sendMessage(ChatColor.GOLD + "Starting Electric Engines (READY TO DIVE)");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
					c.submergedMode = true;

					for (int eng : c.engineIDIsOn.keySet()) {
						int engineType = c.engineIDTypes.get(eng);
						if ((engineType != 0) && (engineType != 1) && (engineType != 2) && (engineType != 4) && (engineType != 9)) {
							c.engineIDIsOn.put(eng, false);
						}
					}


					for (String s : c.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.DARK_AQUA + "DIVE! DIVE!");
						}
					}
					divingBellThread(sign.getBlock().getLocation());
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the sub before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("ballasttanks")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				c.ballastMode = (c.ballastMode + 1) % 4;
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);

			} else {
				player.sendMessage(ChatColor.RED + "Start the sub before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("firecontrol")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				int tubeNum = 0;
				String tubeString = sign.getLine(1).trim().toLowerCase();
				tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");
				if (!tubeString.isEmpty()) {
					try {
						tubeNum = Integer.parseInt(tubeString);
					} catch (NumberFormatException nfe) {
						tubeNum = 0;
					}
				}
				if ((tubeNum != 0) && c.tubeFiringMode.containsKey(tubeNum)) {
					if (leftClick) {
						player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
						if (c.tubeFiringMode.get(tubeNum) == -3) {
							if (c.tubeFiringDisplay.get(tubeNum) == 0) {
								if (c.tubeFiringAuto.get(tubeNum)) {
									player.sendMessage(ChatColor.RED + "Cannot change depth in auto mode.");
								} else {
									if (player.isSneaking()) {
										c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 5));
									} else {
										c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 1));
									}
									if (c.tubeFiringDepth.get(tubeNum) > 60) {
										c.tubeFiringDepth.put(tubeNum, 0);
									}
								}
							} else if (c.tubeFiringDisplay.get(tubeNum) == 1) {
								c.tubeFiringArmed.put(tubeNum, !c.tubeFiringArmed.get(tubeNum));
							} else if (c.tubeFiringDisplay.get(tubeNum) == 2) {
								c.tubeFiringAuto.put(tubeNum, !c.tubeFiringAuto.get(tubeNum));
							}
						} else if (c.tubeFiringMode.get(tubeNum) == -2) {

							if (c.tubeFiringDisplay.get(tubeNum) == 0) {
								if (player.isSneaking()) {
									c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 5));
								} else {
									c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 1));
								}
								if (c.tubeFiringDepth.get(tubeNum) > 60) {
									c.tubeFiringDepth.put(tubeNum, 0);
								}
							} else if (c.tubeFiringDisplay.get(tubeNum) == 1) {
								if (player.isSneaking()) {
									c.tubeFiringArm.put(tubeNum, (c.tubeFiringArm.get(tubeNum) + 50));
								} else {
									c.tubeFiringArm.put(tubeNum, (c.tubeFiringArm.get(tubeNum) + 10));
								}
								if (c.tubeFiringArm.get(tubeNum) > 250) {
									c.tubeFiringArm.put(tubeNum, 20);
								}
							}
						} else if (c.tubeFiringMode.get(tubeNum) == -1) {
							if (c.tubeFiringDisplay.get(tubeNum) == 0) {
								if (player.isSneaking()) {
									c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 5));
								} else {
									c.tubeFiringDepth.put(tubeNum, (c.tubeFiringDepth.get(tubeNum) + 1));
								}
								if (c.tubeFiringDepth.get(tubeNum) > 60) {
									c.tubeFiringDepth.put(tubeNum, 0);
								}
							} else if (c.tubeFiringDisplay.get(tubeNum) == 1) {
								if (player.isSneaking()) {
									c.tubeFiringArm.put(tubeNum, (c.tubeFiringArm.get(tubeNum) + 50));
								} else {
									c.tubeFiringArm.put(tubeNum, (c.tubeFiringArm.get(tubeNum) + 10));
								}
								if (c.tubeFiringArm.get(tubeNum) > 250) {
									c.tubeFiringArm.put(tubeNum, 20);
								}
							}
						}

					} else // right click
					{
						player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
						if (player.isSneaking()) {
							if (c.tubeFiringMode.get(tubeNum) == -3) {
								player.sendMessage(ChatColor.RED + "Cannot change mode while torpedo is live.");
							} else if (c.tubeFiringMode.get(tubeNum) == -2) {
								c.tubeFiringMode.put(tubeNum, -1);
								c.tubeFiringDisplay.put(tubeNum, 0);

							} else if (c.tubeFiringMode.get(tubeNum) == -1) {
								c.tubeFiringMode.put(tubeNum, -2);

								c.tubeFiringDisplay.put(tubeNum, 0);
							}
						} else {
							if (c.tubeFiringMode.get(tubeNum) == -3) {
								c.tubeFiringDisplay.put(tubeNum, (c.tubeFiringDisplay.get(tubeNum) + 1) % 3);

							} else if (c.tubeFiringMode.get(tubeNum) == -2) {
								c.tubeFiringDisplay.put(tubeNum, (c.tubeFiringDisplay.get(tubeNum) + 1) % 2);

							} else if (c.tubeFiringMode.get(tubeNum) == -1) {
								c.tubeFiringDisplay.put(tubeNum, (c.tubeFiringDisplay.get(tubeNum) + 1) % 2);

							}
						}
					}
				} else if (tubeNum != 0) {
					c.tubeFiringMode.put(tubeNum, -2);
					c.tubeFiringDepth.put(tubeNum, 1);
					c.tubeFiringArm.put(tubeNum, 20);
					c.tubeFiringArmed.put(tubeNum, false);
					c.tubeFiringHeading.put(tubeNum, c.rotation);
					c.tubeFiringAuto.put(tubeNum, true);
					c.tubeFiringRudder.put(tubeNum, 0);
					c.tubeFiringDisplay.put(tubeNum, 0);

				} else {
					player.sendMessage(ChatColor.DARK_RED + "Sign Error");
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("tdc")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (leftClick) {
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

					if (c.tubeMk1FiringDisplay == 0) {
						if (player.isSneaking()) {
							c.tubeMk1FiringDepth += 5;
						} else {
							c.tubeMk1FiringDepth += 1;
						}
						if (c.tubeMk1FiringDepth > 60) {
							c.tubeMk1FiringDepth = 0;
						}
					} else // if( c.tubeMk1FiringDisplay == 1 )
					{
						if (player.isSneaking()) {
							c.tubeMk1FiringSpread -= 5;
						} else {
							c.tubeMk1FiringSpread += 5;
						}
						if ((c.tubeMk1FiringSpread > 30) || (c.tubeMk1FiringSpread < 0)) {
							c.tubeMk1FiringSpread = 0;
						}
					}

				} else // right click
				{
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
					if (player.isSneaking()) {

						if (c.tubeMk1FiringDisplay == -1) {
							c.tubeMk1FiringDisplay = 0;
						} else {
							if (c.tubeMk1FiringMode == -2) {
								c.tubeMk1FiringMode = -1;
							} else if (c.tubeMk1FiringMode == -1) {
								c.tubeMk1FiringMode = -2;
							}
						}
					} else {
						if ((c.tubeMk1FiringDisplay == -1) || (c.tubeMk1FiringDisplay == 1)) {
							c.tubeMk1FiringDisplay = 0;

						} else {
							c.tubeMk1FiringDisplay = 1;

						}
					}
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("radar")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (!c.radarOn) {
					c.radarOn = true;
					player.sendMessage(ChatColor.GREEN + "Radar ACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.0f);
				} else {
					c.radarOn = false;
					player.sendMessage(ChatColor.RED + "Radar DEACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.5f, 1.0f);
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("sonar")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (!c.sonarOn) {
					c.sonarOn = true;
					player.sendMessage(ChatColor.GREEN + "Sonar ACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				} else {
					c.sonarOn = false;
					player.sendMessage(ChatColor.RED + "Sonar DEACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("hfsonar")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (!c.hfOn) {
					c.hfOn = true;
					player.sendMessage(ChatColor.GREEN + "High Frequency Sonar ACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				} else {
					c.hfOn = false;
					player.sendMessage(ChatColor.RED + "High Frequency Sonar DEACTIVATED!");
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("passivesonar")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (!c.sonarTargetIDs.isEmpty()) {
					c.sonarTargetIndex += 1;
					if (c.sonarTargetIndex >= c.sonarTargetIDs.size()) {
						c.sonarTargetIndex = 0;
					}
					while ((c.sonarTargetIndex < c.sonarTargetIDs.size()) && c.sonarTargetIDs2.get(c.sonarTargetIndex).sinking) {
						c.sonarTargetIndex += 1;
					}
					if (c.sonarTargetIndex == c.sonarTargetIDs.size()) // never found
					{
						c.sonarTargetIndex = -1;
						c.sonarTarget = null;
						c.sonarTargetRng = -1;
					} else {
						c.sonarTarget = c.sonarTargetIDs2.get(c.sonarTargetIndex);
						c.sonarTargetRng = -1;
					}

					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("activesonar")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				c.doPing = true;
				player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("flak-gun")) {
			if (!PermissionInterface.CheckPerm(player, "navycraft.basic")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this sign");
				return;
			}
			BlockFace bf = BlockFace.NORTH;

			switch (block.getData()) {
				case (byte) 0x2:// n
					bf = BlockFace.SOUTH;
					break;
				case (byte) 0x3:// s
					bf = BlockFace.NORTH;
					break;
				case (byte) 0x4:// w
					bf = BlockFace.EAST;
					break;
				case (byte) 0x5:// e
					bf = BlockFace.WEST;
					break;
			}

			if (player.getItemInHand().getTypeId() > 0) {
				player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
				return;
			}

			Location newLoc = new Location(player.getWorld(), block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockX() + 0.5, block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockY(), block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockZ() + 0.5);
			player.teleport(newLoc);

			player.setItemInHand(new ItemStack(369, 1));
			NavyCraft.flakGunnersList.add(player);
			player.sendMessage(ChatColor.YELLOW + "Manning Flak-Gun! Left Click with Blaze Rod to fire!");
			player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

		} else if (craftTypeName.equalsIgnoreCase("aa-gun")) {
			if (!PermissionInterface.CheckPerm(player, "navycraft.basic")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this sign");
				return;
			}
			BlockFace bf = BlockFace.NORTH;

			switch (block.getData()) {
				case (byte) 0x2:// n
					bf = BlockFace.SOUTH;
					break;
				case (byte) 0x3:// s
					bf = BlockFace.NORTH;
					break;
				case (byte) 0x4:// w
					bf = BlockFace.EAST;
					break;
				case (byte) 0x5:// e
					bf = BlockFace.WEST;
					break;
			}

			if (player.getItemInHand().getTypeId() > 0) {
				player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
				return;
			}

			Location newLoc = new Location(player.getWorld(), block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockX() + 0.5, block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockY(), block.getRelative(bf).getRelative(BlockFace.UP).getLocation().getBlockZ() + 0.5);
			player.teleport(newLoc);

			player.setItemInHand(new ItemStack(369, 1));
			NavyCraft.aaGunnersList.add(player);
			player.sendMessage(ChatColor.YELLOW + "Manning AA-Gun! Left Click with Blaze Rod to fire!");
			player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);


		} else if (craftTypeName.equalsIgnoreCase("launcher")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (!c.launcherOn) {
					if ((c.speed == 0) && (c.setSpeed == 0) && (c.driverName == null)) {
						c.launcherOn = true;
						player.sendMessage(ChatColor.GREEN + "Vehicle launcher armed!");
					} else {
						player.sendMessage(ChatColor.RED + "Come to full stop and release helm before launching vehicles.");
					}
				} else {
					c.launcherOn = false;
					player.sendMessage(ChatColor.RED + "Vehicle launcher disarmed!");
				}
				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
				player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("radio")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				if (player.isSneaking()) {
					if (leftClick) {
						c.radioSelector = c.radioSelector - 1;
						if (c.radioSelector == 0) {
							c.radioSelector = 5;
						}
					} else {
						c.radioSelector = c.radioSelector + 1;
						if (c.radioSelector == 6) {
							c.radioSelector = 1;
						}
					}
				} else {
					if (c.radioSelector == 1) {
						if (!leftClick) {
							c.radio1 = (c.radio1 + 1);
							if (c.radio1 > 9) {
								c.radio1 = 0;
							}
						} else {
							c.radio1 = (c.radio1 - 1);
							if (c.radio1 < 0) {
								c.radio1 = 9;
							}
						}
					} else if (c.radioSelector == 2) {
						if (!leftClick) {
							c.radio2 = (c.radio2 + 1);
							if (c.radio2 > 9) {
								c.radio2 = 0;
							}
						} else {
							c.radio2 = (c.radio2 - 1);
							if (c.radio2 < 0) {
								c.radio2 = 9;
							}
						}
					} else if (c.radioSelector == 3) {
						if (!leftClick) {
							c.radio3 = (c.radio3 + 1);
							if (c.radio3 > 9) {
								c.radio3 = 0;
							}
						} else {
							c.radio3 = (c.radio3 - 1);
							if (c.radio3 < 0) {
								c.radio3 = 9;
							}
						}
					} else if (c.radioSelector == 4) {
						if (!leftClick) {
							c.radio4 = (c.radio4 + 1);
							if (c.radio4 > 9) {
								c.radio4 = 0;
							}
						} else {
							c.radio4 = (c.radio4 - 1);
							if (c.radio4 < 0) {
								c.radio4 = 9;
							}
						}
					} else if (c.radioSelector == 5) {
						c.radioSetOn = !c.radioSetOn;
						if (c.radioSetOn) {
							player.sendMessage(ChatColor.GREEN + "Radio turned on.");
						} else {
							player.sendMessage(ChatColor.RED + "Radio turned off.");
						}
					}
				}

				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);
				player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

			} else {
				player.sendMessage(ChatColor.RED + "Start the vehicle before using this sign.");
			}
		} else if (craftTypeName.equalsIgnoreCase("engine")) {
			Craft c = Craft.getCraft(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
			if ((c != null) && (Craft.getPlayerCraft(player) == c) && c.isDressed(player)) {
				String engineNumStr = sign.getLine(1).trim().toLowerCase();
				engineNumStr = engineNumStr.replaceAll(ChatColor.BLUE.toString(), "");
				int engNum = -1;
				if (!engineNumStr.isEmpty()) {
					try {
						engNum = Integer.parseInt(engineNumStr);
					} catch (NumberFormatException nfe) {
						engNum = -1;
					}
				}

				if ((engNum > -1) && c.engineIDLocs.containsKey(engNum)) {
					if (c.engineIDIsOn.get(engNum)) {
						player.sendMessage(ChatColor.RED + "Stopping Engine: " + ChatColor.YELLOW + engNum + "!");
						c.engineIDIsOn.put(engNum, false);
						c.engineIDSetOn.put(engNum, false);
					} else if (c.submergedMode) {
						if ((c.engineIDTypes.get(engNum) != 0) && (c.engineIDTypes.get(engNum) != 1) && (c.engineIDTypes.get(engNum) != 2) && (c.engineIDTypes.get(engNum) != 4) && (c.engineIDTypes.get(engNum) != 9)) {
							player.sendMessage(ChatColor.RED + "Cannot start this engine while set to dive!");
						} else {
							player.sendMessage(ChatColor.GREEN + "Starting Engine: " + ChatColor.YELLOW + engNum + "!");
							c.engineIDIsOn.put(engNum, true);
							c.engineIDSetOn.put(engNum, true);
						}
					} else {
						player.sendMessage(ChatColor.GREEN + "Starting Engine: "+ ChatColor.YELLOW + engNum + "!");
						c.engineIDIsOn.put(engNum, true);
						c.engineIDSetOn.put(engNum, true);
					}
					player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				}

				CraftMover cm = new CraftMover(c, plugin);
				cm.signUpdates(block);

			} else {
				player.sendMessage("Start the vehicle before using this sign.");
			}
		} else {

			// if the first line of the sign is a craft type, get the matching craft type.
			CraftType craftType = CraftType.getCraftType(craftTypeName);

			// it is a registred craft type !
			if ((craftType != null) || craftTypeName.equalsIgnoreCase("helm")) {

				if (NavyCraft.checkNoDriveRegion(player.getLocation())) {
					player.sendMessage(ChatColor.RED + "You do not have permission to drive vehicles in this area. Please use a spawner.");
					return;
				}
				Craft testCraft = Craft.getCraft(block.getX(), block.getY(), block.getZ());

				if ((testCraft != null) && ((testCraft.captainName == null) || testCraft.abandoned || (testCraft.captainAbandoned && !craftTypeName.equalsIgnoreCase("helm")))) {
					// check restrictions


					if (!PermissionInterface.CheckPerm(player, "navycraft." + craftType.name)) {
						player.sendMessage(ChatColor.RED + "You do not have permission to use this type of vehicle.");
						return;
					}

					if (testCraft.isAutoCraft && !player.isOp()) {
						player.sendMessage(ChatColor.RED + "You cannot drive an auto-pilot vehicle.");
						return;
					}
					if (player.getItemInHand().getTypeId() > 0) {
						player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
						return;
					}

					if (craftTypeName.equalsIgnoreCase("helm")) {
						player.sendMessage(ChatColor.RED + "There is no captain. Use main vehicle sign.");
						return;
					}


					if (testCraft.abandoned && (testCraft.captainName != null) && (player.getName() != testCraft.captainName)) {
						if (!testCraft.takingOver) {
							Craft.takeoverTimerThread(player, testCraft);
						}
						player.sendMessage(ChatColor.YELLOW + "This vehicle will become abandoned in 2 minutes.");
						return;
					} else if (testCraft.captainAbandoned && testCraft.crewNames.contains(player.getName()) && (testCraft.captainName != null)) {
						testCraft.captainName = player.getName();
						testCraft.captainAbandoned = false;
						player.sendMessage(ChatColor.GREEN + "You take command of the vehicle.");
						for (String s : testCraft.crewNames) {
							Player p = plugin.getServer().getPlayer(s);
							if ((p != null) && (s != player.getName())) {
								p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + player.getName() + " takes command of your crew.");
							}
						}
						return;
					}

					player.setItemInHand(new ItemStack(283, 1));

					testCraft.buildCrew(player, false);
					if (testCraft.customName != null) {
						player.sendMessage(ChatColor.GOLD + "You take command of the " + ChatColor.WHITE + testCraft.customName.toUpperCase() + ChatColor.GOLD + " class!");
					} else {
						player.sendMessage(ChatColor.GOLD + "You take command of the " + ChatColor.WHITE + testCraft.name.toUpperCase() + ChatColor.GOLD + " class!");
					}
					player.sendMessage(ChatColor.GOLD + "You take control of the helm.");
					testCraft.haveControl = true;
					testCraft.launcherOn = false;

					Location newLoc;
					if ((block.getRelative(BlockFace.DOWN).getTypeId() != 0) && (block.getRelative(BlockFace.DOWN).getTypeId() != 68)) {
						newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, block.getLocation().getBlockY(), block.getLocation().getBlockZ() + 0.5);
					} else {
						newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, (double) block.getLocation().getBlockY() - 1, block.getLocation().getBlockZ() + 0.5);
					}
					newLoc.setYaw(player.getLocation().getYaw());
					player.teleport(newLoc);

					CraftMover cm = new CraftMover(testCraft, plugin);
					cm.structureUpdate(null, false);

					if (craftType != testCraft.type) {
						testCraft.type = craftType;
					}
					return;
				} else if ((testCraft != null) && !testCraft.launcherOn) /// set driver
				{
					if ((testCraft.driverName != null) && (testCraft.driverName != player.getName())) {
						player.sendMessage(testCraft.driverName + ChatColor.GOLD + " already has the helm.");
					} else if ((testCraft.driverName != null) && (testCraft.driverName == player.getName())) {
						player.sendMessage(ChatColor.GOLD + "Avoid clicking on the sign while driving.");

						return;
					} else {
						if (player.getItemInHand().getTypeId() > 0) {
							player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
							return;
						}

						if (playerCraft != testCraft) {
							player.sendMessage(ChatColor.RED + "You are not on this crew.");

							return;
						}

						if (!testCraft.isDressed(player)) { return; }
						if ((testCraft.type != craftType) && !craftTypeName.equalsIgnoreCase("helm")) {
							player.sendMessage(ChatColor.RED + "Vehicle sign differs from class, Use" + ChatColor.YELLOW + "/ship release.");
							return;
						}

						testCraft.driverName = player.getName();
						player.sendMessage(ChatColor.GOLD + "You take control of the helm.");
						testCraft.haveControl = true;
						if ((craftType != null) && (craftType != testCraft.type)) {
							testCraft.type = craftType;
						}

						Location newLoc;
						if ((block.getRelative(BlockFace.DOWN).getTypeId() != 0) && (block.getRelative(BlockFace.DOWN).getTypeId() != 68)) {
							newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, block.getLocation().getBlockY(), block.getLocation().getBlockZ() + 0.5);
						} else {
							newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, (double) block.getLocation().getBlockY() - 1, block.getLocation().getBlockZ() + 0.5);
						}
						newLoc.setYaw(player.getLocation().getYaw());
						player.teleport(newLoc);
						player.setItemInHand(new ItemStack(283, 1));

						CraftMover cm = new CraftMover(testCraft, plugin);
						cm.structureUpdate(null, false);
					}
					return;
				} else if (testCraft != null) // launcher is on
				{
					if ((craftType == testCraft.type) || craftTypeName.equalsIgnoreCase("helm")) {
						player.sendMessage(ChatColor.RED + "Cannot use main vehicle sign, helm sign, or sign of same type while launcher is armed.");
						return;
					} else if (testCraft.speed != 0) {
						player.sendMessage(ChatColor.RED + "Cannot launch vehicles while main vehicle is moving.");
						return;
					}
					/// continue below to launch new vehicle!
				} else if (craftTypeName.equalsIgnoreCase("helm")) {
					player.sendMessage(ChatColor.RED + "Start the craft first. Use main vehicle sign.");
					return;
				}


				if (!PermissionInterface.CheckPerm(player, "navycraft." + craftType.name)) {
					player.sendMessage(ChatColor.RED + "You do not have permission to use this type of vehicle.");
					return;
				}

				if (player.getItemInHand().getTypeId() > 0) {
					player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
					return;
				}

				String name = sign.getLine(1);// .replaceAll("§.", "");

				if (name.trim().equals("")) {
					name = null;
				}

				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();

				int direction = block.getData();

				// get the block the sign is attached to
				x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
				z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

				float dr = 0;

				switch (block.getData()) {
					case (byte) 0x2:// n
						dr = 180;
						break;
					case (byte) 0x3:// s
						dr = 0;
						break;
					case (byte) 0x4:// w
						dr = 90;
						break;
					case (byte) 0x5:// e
						dr = 270;
						break;
				}
				player.setItemInHand(new ItemStack(283, 1));
				Craft theCraft = NavyCraft.instance.createCraft(player, craftType, x, y, z, name, dr, block, false);


				if (theCraft != null) {
					Location newLoc;
					if ((block.getRelative(BlockFace.DOWN).getTypeId() != 0) && (block.getRelative(BlockFace.DOWN).getTypeId() != 68)) {
						newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, block.getLocation().getBlockY(), block.getLocation().getBlockZ() + 0.5);
					} else {
						newLoc = new Location(player.getWorld(), block.getLocation().getBlockX() + 0.5, (double) block.getLocation().getBlockY() - 1, block.getLocation().getBlockZ() + 0.5);
					}
					newLoc.setYaw(player.getLocation().getYaw());
					player.teleport(newLoc);

					CraftMover cm = new CraftMover(theCraft, plugin);
					cm.structureUpdate(null, false);
					if (sign.getLine(3).equalsIgnoreCase("center")) {
					}
				} else {
					player.setItemInHand(null);
				}

				return;
			} else if (craftTypeName.equalsIgnoreCase("engage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
				if (playerCraft == null) {
					player.kickPlayer("Don't.");
					return;
				}
				Craft_Hyperspace.enterHyperSpace(playerCraft);
				sign.setLine(0, "Disengage Hyperdrive");
			} else if (craftTypeName.equalsIgnoreCase("disengage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
				if (playerCraft == null) {
					player.kickPlayer("Are you fucking retarded?");
					return;
				}
				Craft_Hyperspace.exitHyperSpace(playerCraft);
				sign.setLine(0, "Engage Hyperdrive");
			}
		}
	}

	public static Player matchPlayerName(String subName) {
		Set<Player> playersOnline = new HashSet<>();
		playersOnline.addAll(NavyCraft.instance.getServer().getOnlinePlayers());
		ArrayList<Player> userList = new ArrayList<>();

		for (Player p : playersOnline) {
			if (p.getName().contains(subName)) {
				userList.add(p);
			}
		}

		if (userList.size() == 1) {
			return userList.get(0);
		} else {
			System.out.println("Attempted to find player matching " + subName + " but failed.");
			return null;
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		NavyCraft.instance.DebugMessage("A SIGN CHANGED!", 3);

		Player player = event.getPlayer();
		String craftTypeName = event.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

		// remove brackets and stars
		if (craftTypeName.startsWith("[")) {
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);
		}
		if (craftTypeName.startsWith("*")) {
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);
		}

		// if the first line of the sign is a craft type, get the matching craft type.
		CraftType craftType = CraftType.getCraftType(craftTypeName);

		if (!player.isOp() && (((craftType != null) || craftTypeName.equalsIgnoreCase("flak-gun") || craftTypeName.equalsIgnoreCase("helm") || craftTypeName.equalsIgnoreCase("periscope") || craftTypeName.equalsIgnoreCase("nav") || craftTypeName.equalsIgnoreCase("aa-gun") || craftTypeName.equalsIgnoreCase("select") || craftTypeName.equalsIgnoreCase("claim") || craftTypeName.equalsIgnoreCase("spawn") || craftTypeName.equalsIgnoreCase("recall") || craftTypeName.equalsIgnoreCase("target") || craftTypeName.equalsIgnoreCase("radar") || craftTypeName.equalsIgnoreCase("detector") || craftTypeName.equalsIgnoreCase("sonar") || craftTypeName.equalsIgnoreCase("hydrophone") || craftTypeName.equalsIgnoreCase("subdrive") || craftTypeName.equalsIgnoreCase("firecontrol") || craftTypeName.equalsIgnoreCase("passivesonar") || craftTypeName.equalsIgnoreCase("activesonar") || craftTypeName.equalsIgnoreCase("hfsonar") || craftTypeName.equalsIgnoreCase("launcher") || craftTypeName.equalsIgnoreCase("engine") || craftTypeName.equalsIgnoreCase("tdc") || craftTypeName.equalsIgnoreCase("radio")) && !PermissionInterface.CheckPerm(player, "navycraft.signcreate"))) {
			player.sendMessage(ChatColor.RED + "You don't have permission to create this type of sign!");
			event.setCancelled(true);
			return;
		}
		
		if (!player.isOp() && craftType != null && craftType.adminBuild && !PermissionInterface.CheckPerm(player, "navycraft.admin") ) {
			player.sendMessage(ChatColor.RED + "You don't have permission to create this type of sign!");
			event.setCancelled(true);
			return;
		}
		

		Craft theCraft = Craft.getPlayerCraft(event.getPlayer());
		// System.out.println("Updated craft is " + updatedCraft.name + " of type " + updatedCraft.type.name);

		theCraft = Craft.getCraft(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
		if (theCraft != null) {
			if (((craftTypeName.equalsIgnoreCase("flak-gun") || craftTypeName.equalsIgnoreCase("helm") || craftTypeName.equalsIgnoreCase("periscope") || craftTypeName.equalsIgnoreCase("nav") || craftTypeName.equalsIgnoreCase("aa-gun") || craftTypeName.equalsIgnoreCase("select") || craftTypeName.equalsIgnoreCase("spawn") || craftTypeName.equalsIgnoreCase("recall") || craftTypeName.equalsIgnoreCase("target") || craftTypeName.equalsIgnoreCase("radar") || craftTypeName.equalsIgnoreCase("detector") || craftTypeName.equalsIgnoreCase("sonar") || craftTypeName.equalsIgnoreCase("hydrophone") || craftTypeName.equalsIgnoreCase("subdrive") || craftTypeName.equalsIgnoreCase("firecontrol") || craftTypeName.equalsIgnoreCase("passivesonar") || craftTypeName.equalsIgnoreCase("activesonar") || craftTypeName.equalsIgnoreCase("hfsonar") || craftTypeName.equalsIgnoreCase("launcher") || craftTypeName.equalsIgnoreCase("engine") || craftTypeName.equalsIgnoreCase("tdc") || craftTypeName.equalsIgnoreCase("radio")))) {
				player.sendMessage(ChatColor.RED + "You cannot create this sign on a running vehicle");
				event.setCancelled(true);
				return;
			}
		}
		// }

		if (PermissionInterface.CheckEnabledWorld(player.getLocation()) && ((craftTypeName.equalsIgnoreCase("flak-gun") || craftTypeName.equalsIgnoreCase("helm") || craftTypeName.equalsIgnoreCase("nav") || craftTypeName.equalsIgnoreCase("periscope") || craftTypeName.equalsIgnoreCase("aa-gun") || craftTypeName.equalsIgnoreCase("radar") || craftTypeName.equalsIgnoreCase("detector") || craftTypeName.equalsIgnoreCase("sonar") || craftTypeName.equalsIgnoreCase("hydrophone") || craftTypeName.equalsIgnoreCase("subdrive") || craftTypeName.equalsIgnoreCase("firecontrol") || craftTypeName.equalsIgnoreCase("passivesonar") || craftTypeName.equalsIgnoreCase("activesonar") || craftTypeName.equalsIgnoreCase("hfsonar") || craftTypeName.equalsIgnoreCase("launcher") || craftTypeName.equalsIgnoreCase("engine") || craftTypeName.equalsIgnoreCase("tdc") || craftTypeName.equalsIgnoreCase("radio")))) {
			int cost = 0;
			if (craftTypeName.equalsIgnoreCase("helm")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("nav")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("periscope")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("aa-gun")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("flak-gun")) {
				cost = 200;
			} else if (craftTypeName.equalsIgnoreCase("radio")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("radar")) {
				cost = 200;
			} else if (craftTypeName.equalsIgnoreCase("detector")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("sonar")) {
				cost = 250;
			} else if (craftTypeName.equalsIgnoreCase("hydrophone")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("subdrive")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("firecontrol")) {
				cost = 1000;
			} else if (craftTypeName.equalsIgnoreCase("tdc")) {
				cost = 400;
			} else if (craftTypeName.equalsIgnoreCase("passivesonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("activesonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("hfsonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("engine")) {
				String engineTypeStr = event.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
				if (engineTypeStr != null) {
					if (engineTypeStr.equalsIgnoreCase("Diesel 1")) {
						cost = 100;
					}
					if (engineTypeStr.equalsIgnoreCase("Motor 1")) {
						cost = 150;
					}
					if (engineTypeStr.equalsIgnoreCase("Diesel 2")) {
						cost = 250;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 1")) {
						cost = 250;
					}
					if (engineTypeStr.equalsIgnoreCase("Diesel 3")) {
						cost = 1000;
					}
					if (engineTypeStr.equalsIgnoreCase("Gasoline 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 2")) {
						cost = 600;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 3")) {
						cost = 1250;
					}
					if (engineTypeStr.equalsIgnoreCase("Gasoline 2")) {
						cost = 100;
					}
					if (engineTypeStr.equalsIgnoreCase("Nuclear")) {
						cost = 10000;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 2")) {
						cost = 80;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 3")) {
						cost = 120;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 4")) {
						cost = 160;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 7")) {
						cost = 500;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 5")) {
						cost = 400;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 6")) {
						cost = 500;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 8")) {
						cost = 5000;
					}
					if (engineTypeStr.equalsIgnoreCase("Tank 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Tank 2")) {
						cost = 5000;
					}
				}
			}

			if (cost > 0) {
				Essentials ess;
				ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
				if (ess == null) {
					player.sendMessage("Essentials Economy error");
					return;
				}
				if (!PermissionInterface.CheckQuietPerm(player, "navycraft.free") && !ess.getUser(player).canAfford(new BigDecimal(cost))) {
					player.sendMessage(ChatColor.YELLOW + "You cannot afford this sign:" + ChatColor.RED + "$" + cost);
					event.setCancelled(true);
					return;
				}else if( !PermissionInterface.CheckQuietPerm(player, "navycraft.free") ) {
					ess.getUser(player).takeMoney(new BigDecimal(cost));
					player.sendMessage(ChatColor.YELLOW + "You purchase sign for " + ChatColor.GREEN + "$" + cost + ChatColor.YELLOW + ". Type " + ChatColor.WHITE + "\"/sign undo\"" + ChatColor.YELLOW + " to cancel.");
				}else
				{
					player.sendMessage(ChatColor.YELLOW + "You purchase sign for " + ChatColor.GREEN + "FREE" + ChatColor.YELLOW + ". Type " + ChatColor.WHITE + "\"/sign undo\"" + ChatColor.YELLOW + " to cancel.");
				}
				NavyCraft.playerLastBoughtSign.put(player, event.getBlock());
				NavyCraft.playerLastBoughtCost.put(player, cost);
				NavyCraft.playerLastBoughtSignString0.put(player, craftTypeName);
				String string1 = event.getLine(1).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
				NavyCraft.playerLastBoughtSignString1.put(player, string1);
				String string2 = event.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
				NavyCraft.playerLastBoughtSignString2.put(player, string2);
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPhysics(final BlockPhysicsEvent event) {
		if (!event.isCancelled()) {

			final Block block = event.getBlock();
			if (Craft_Hyperspace.hyperspaceBlocks.contains(block)) {
				event.setCancelled(true);
			}

			
			if ((block.getTypeId() == 63) || (block.getTypeId() == 68) || (block.getTypeId() == 50) || (block.getTypeId() == 75) || (block.getTypeId() == 76) || (block.getTypeId() == 65) || (block.getTypeId() == 64) || (block.getTypeId() == 71) || (block.getTypeId() == 70) || (block.getTypeId() == 72) || (block.getTypeId() == 143)) {
				Craft c = Craft.getCraft(block.getX(), block.getY(), block.getZ());
				if (c != null) {

					// if not iron door being controlled by circuit...
					if ((event.getChangedTypeId() != 0) && !(((block.getTypeId() == 71) || (block.getTypeId() == 64)) && ((event.getChangedTypeId() == 69) || (event.getChangedTypeId() == 77) || (event.getChangedTypeId() == 55) || (event.getChangedTypeId() == 70) || (event.getChangedTypeId() == 72) || (block.getTypeId() == 143) || (block.getTypeId() == 75) || (block.getTypeId() == 76) || (block.getTypeId() == 50)))) {

						event.setCancelled(true);
					}
				}
			}
			
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockFromTo(final BlockFromToEvent event) {
		if (!event.isCancelled()) {
			final Block block = event.getToBlock();

			if ((block.getTypeId() == 75) || (block.getTypeId() == 76) || (block.getTypeId() == 65) || (block.getTypeId() == 69) || (block.getTypeId() == 77) || (block.getTypeId() == 70) || (block.getTypeId() == 72) || (block.getTypeId() == 68) || (block.getTypeId() == 63) || (block.getTypeId() == 143) || (block.getTypeId() == 55)) {
				if (Craft.getCraft(block.getX(), block.getY(), block.getZ()) != null) {
					// event.setCancelled(true);
					block.setTypeId(8);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		int blockId = event.getBlock().getTypeId();
		Location loc = event.getBlock().getLocation();
		// System.out.println(blockId);

		if ((blockId == 29) || (blockId == 33)) { // piston / sticky piston (base)
			Craft craft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

			if (craft != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage("You just did something with a piston, didn't you?");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if( p.getWorld().getName().equalsIgnoreCase("warworld2") && p.getGameMode() != GameMode.CREATIVE )
		{
			if( NavyCraft_PlayerListener.checkForTarget(event.getBlock()) )
			{
				p.sendMessage(ChatColor.RED + "This sign can only be destroyed by an explosive!");
				event.setCancelled(true);
				return;
			}else
			{
				Block checkBlock;
				checkBlock = event.getBlock().getRelative(BlockFace.NORTH);
				if( checkBlock.getTypeId() == 68 && NavyCraft_PlayerListener.checkForTarget(checkBlock) )
				{
					p.sendMessage(ChatColor.RED + "This sign can only be destroyed by an explosive!");
					event.setCancelled(true);
					return;
				}
				checkBlock = event.getBlock().getRelative(BlockFace.SOUTH);
				if( checkBlock.getTypeId() == 68 && NavyCraft_PlayerListener.checkForTarget(checkBlock) )
				{
					p.sendMessage(ChatColor.RED + "This sign can only be destroyed by an explosive!");
					event.setCancelled(true);
					return;
				}
				checkBlock = event.getBlock().getRelative(BlockFace.EAST);
				if( checkBlock.getTypeId() == 68 && NavyCraft_PlayerListener.checkForTarget(checkBlock) )
				{
					p.sendMessage(ChatColor.RED + "This sign can only be destroyed by an explosive!");
					event.setCancelled(true);
					return;
				}
				checkBlock = event.getBlock().getRelative(BlockFace.WEST);
				if( checkBlock.getTypeId() == 68 && NavyCraft_PlayerListener.checkForTarget(checkBlock) )
				{
					p.sendMessage(ChatColor.RED + "This sign can only be destroyed by an explosive!");
					event.setCancelled(true);
					return;
				}
			}
		}else if( p.getWorld().getName().equalsIgnoreCase("warworld1") )
		{
			Block checkBlock;
			checkBlock = event.getBlock();
			int craftBlockId = checkBlock.getTypeId();

			Craft checkCraft = Craft.getCraft(checkBlock.getX(), checkBlock.getY(), checkBlock.getZ());

			if (checkCraft != null) {
				if ((craftBlockId == 46) && (p.getGameMode() != GameMode.CREATIVE)) {
					p.sendMessage(ChatColor.RED + "Can't break vehicle TNT.");
					event.setCancelled(true);
					return;
				} else if ((craftBlockId == 75) || (craftBlockId == 76) || (craftBlockId == 65) || (craftBlockId == 68) || (craftBlockId == 63) || (craftBlockId == 69) || (craftBlockId == 77) || (craftBlockId == 70) || (craftBlockId == 72) || (craftBlockId == 55) || (craftBlockId == 143) || (craftBlockId == 64) || (craftBlockId == 71)) {
					int arrayX = checkBlock.getX() - checkCraft.minX;
					int arrayY = checkBlock.getY() - checkCraft.minY;
					int arrayZ = checkBlock.getZ() - checkCraft.minZ;
					checkCraft.matrix[arrayX][arrayY][arrayZ] = -1;

					if (((craftBlockId == 64) && (checkBlock.getRelative(BlockFace.UP).getTypeId() == 64)) || ((craftBlockId == 71) && (checkBlock.getRelative(BlockFace.UP).getTypeId() == 71))) {
						checkBlock.getRelative(BlockFace.UP).setTypeId(0);
						checkCraft.matrix[arrayX][arrayY + 1][arrayZ] = -1;
					}
					if (((craftBlockId == 64) && (checkBlock.getRelative(BlockFace.DOWN).getTypeId() == 64)) || ((craftBlockId == 71) && (checkBlock.getRelative(BlockFace.DOWN).getTypeId() == 71))) {
						checkBlock.getRelative(BlockFace.DOWN).setTypeId(0);
						checkCraft.matrix[arrayX][arrayY - 1][arrayZ] = -1;
					}
					for (DataBlock complexBlock : checkCraft.complexBlocks) {
						if (complexBlock.locationMatches(arrayX, arrayY, arrayZ)) {
							checkCraft.complexBlocks.remove(complexBlock);
							break;
						}
					}
					for (DataBlock dataBlock : checkCraft.dataBlocks) {
						if (dataBlock.locationMatches(arrayX, arrayY, arrayZ)) {
							checkCraft.dataBlocks.remove(dataBlock);
							break;
						}
					}
				}
			}
		}
	}

	public static void divingBellThread(final Location loc) {
		new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				// taskNum = -1;
				try {
					World cw = loc.getWorld();
					for (int i = 0; i < 8; i++) {
						sleep(200);
						if ((i % 2) == 0) {
							cw.playSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 1.2f);
						} else {
							cw.playSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 1);
						}

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start(); // , 20L);
	}

	public static void surfaceBellThread(final Location loc) {
		new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				// taskNum = -1;
				try {
					World cw = loc.getWorld();
					for (int i = 0; i < 2; i++) {
						sleep(300);
						cw.playSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 2);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static boolean checkSpawnerClear(Player player, Block block, BlockFace bf, BlockFace bf2) {
		int shiftRight = 0;
		int shiftForward = 0;
		int shiftUp = 0;
		int shiftDown = 0;
		if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("DD") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP1")) {
			shiftRight = 12;
			shiftForward = 28;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SUB1") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP2")) {
			shiftRight = 8;
			shiftForward = 43;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SUB2") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP3")) {
			shiftRight = 10;
			shiftForward = 70;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("CL") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP4")) {
			shiftRight = 16;
			shiftForward = 55;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("CA") || Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP5")) {
			shiftRight = 16;
			shiftForward = 98;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("HANGAR1")) {
			shiftRight = 16;
			shiftForward = 19;
			shiftUp = 7;
			shiftDown = 0;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("HANGAR2")) {
			shiftRight = 24;
			shiftForward = 32;
			shiftUp = 7;
			shiftDown = 0;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK1")) {
			shiftRight = 11;
			shiftForward = 19;
			shiftUp = 7;
			shiftDown = 0;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK2")) {		
			shiftRight = 25;
			shiftForward = 33;
			shiftUp = 9;
			shiftDown = 0;
		} else {
			player.sendMessage("Unknown lot type error2!");
		}

		Block rightLimit = block.getRelative(bf2, shiftRight).getRelative(bf, shiftForward).getRelative(BlockFace.UP, shiftUp);
		Block leftLimit = block.getRelative(bf, 1).getRelative(BlockFace.DOWN, shiftDown);
		int rightX, rightY, rightZ;
		int leftX, leftY, leftZ;
		rightX = rightLimit.getX();
		rightY = rightLimit.getY();
		rightZ = rightLimit.getZ();
		leftX = leftLimit.getX();
		leftY = leftLimit.getY();
		leftZ = leftLimit.getZ();
		int startX, endX, startZ, endZ;
		if (rightX < leftX) {
			startX = rightX;
			endX = leftX;
		} else {
			startX = leftX;
			endX = rightX;
		}
		if (rightZ < leftZ) {
			startZ = rightZ;
			endZ = leftZ;
		} else {
			startZ = leftZ;
			endZ = rightZ;
		}

		for (Craft c : Craft.craftList) {
			if (c.world == block.getWorld()) {
				if ((c.maxX >= startX) && (c.minX <= endX)) {
					if ((c.maxZ >= startZ) && (c.minZ <= endZ)) {
						if ((c.maxY >= leftY) && (c.minY <= rightY)) { return false; }
					}
				}
			}
		}
		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void inventoryClickEvent(final InventoryClickEvent event) {
		if (!event.isCancelled()) {
			if ( PermissionInterface.CheckEnabledWorld(event.getWhoClicked().getLocation()) ) {
				if ((event.getInventory().getType() == InventoryType.DISPENSER) && (event.getRawSlot() == 4) && ((event.getCurrentItem().getTypeId() == 388) || (event.getCursor().getTypeId() == 388))) {
					event.setCancelled(true);
				}
			}

		}
	}

	public static void autoSpawnSign(Block spawnSignBlock, String routeID) {
		if (plugin.getServer().getWorlds().get(0).getPlayers().isEmpty()) {
			// System.out.println("Autospawn:No players detected in ww1");
			return;
		}
		Player player = plugin.getServer().getWorlds().get(0).getPlayers().get(0);

		int spawnType = 0;
		if (spawnSignBlock == null) {
			int newLastSpawn = 0;
			do {
				double rand = Math.random();
				if (rand < 0.15) {
					newLastSpawn = 0;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-1490, 64, 1525);
					if (rand < 0.05) {
						routeID = "AB1";
					} else if (rand < 0.10) {
						routeID = "AB2";
					} else {
						routeID = "AB3";
					}
				} else if (rand < 0.30) {
					newLastSpawn = 0;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-1490, 64, 1525);
					if (rand < 0.20) {
						routeID = "AC1";
					} else if (rand < 0.25) {
						routeID = "AC2";
					} else {
						routeID = "AC3";
					}
				} else if (rand < 0.45) {
					newLastSpawn = 1;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(1857, 64, -779);
					if (rand < 0.38) {
						routeID = "CB1";
					} else {
						routeID = "CB2";
					}
				} else if (rand < 0.60) {
					newLastSpawn = 1;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(1857, 64, -779);
					if (rand < 0.53) {
						routeID = "CA1";
					} else {
						routeID = "CA2";
					}
				} else if (rand < 0.75) {
					newLastSpawn = 2;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-19, 64, -1661);
					if (rand < 0.68) {
						routeID = "BC1";
					} else {
						routeID = "BC2";
					}
				} else if (rand < 0.90) {
					newLastSpawn = 3;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(1476, 64, 1920);
					if (rand < 0.80) {
						routeID = "D1";
					} else if (rand < 0.85) {
						routeID = "D2";
					} else {
						routeID = "D3";
					}
				} else {
					newLastSpawn = 2;
					spawnSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-19, 64, -1661);
					if (rand < 0.95) {
						routeID = "BA1";
					} else {
						routeID = "BA2";
					}
				}
			} while (lastSpawn == newLastSpawn);
			lastSpawn = newLastSpawn;

		}

		if ((spawnSignBlock == null) || ((spawnSignBlock.getTypeId() != 68) && (spawnSignBlock.getTypeId() != 63))) {
			System.out.println("Autospawn:No sign detected error");
			return;
		}

		Sign sign = (Sign) spawnSignBlock.getState();

		Block block = spawnSignBlock;
		int rotate = -1;
		BlockFace bf, bf2;
		bf = null;
		bf2 = null;
		switch (block.getData()) {
			case (byte) 0x8:// n
				rotate = 180;
				bf = BlockFace.SOUTH;
				bf2 = BlockFace.WEST;
				break;
			case (byte) 0x0:// s
				rotate = 0;
				bf = BlockFace.NORTH;
				bf2 = BlockFace.EAST;
				break;
			case (byte) 0x4:// w
				rotate = 90;
				bf = BlockFace.EAST;
				bf2 = BlockFace.SOUTH;
				break;
			case (byte) 0xC:// e
				rotate = 270;
				bf = BlockFace.WEST;
				bf2 = BlockFace.NORTH;
				break;
			default:
				break;
		}

		if (rotate == -1) { return; }

		boolean isAutoSpawn = false;
		String freeString = sign.getLine(2).trim().toLowerCase();
		freeString = freeString.replaceAll(ChatColor.BLUE.toString(), "");
		if (freeString.equalsIgnoreCase("auto")) {
			isAutoSpawn = true;
		}

		String typeString = sign.getLine(1).trim().toLowerCase();
		typeString = typeString.replaceAll(ChatColor.BLUE.toString(), "");

		wep = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
		if (wep == null) {
			System.out.println("WorldEdit error");
			return;
		}
		EditSession es = wep.createEditSession(player);

		try {
			int oldLimit = es.getBlockChangeLimit();
			es.setBlockChangeLimit(50000);

			Block selectSignBlock;
			int value = 0;
			double rand = Math.random();
			if (rand < 0.6) {
				selectSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-78, 66, 157);
				spawnType = 0;
				value = 400 + (int) (Math.random() * 400);
			} else if (rand < 0.7) {
				selectSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-108, 66, 157);
				spawnType = 1;
				value = 1000 + (int) (Math.random() * 1000);
			} else {
				selectSignBlock = plugin.getServer().getWorlds().get(0).getBlockAt(-96, 66, 157);
				spawnType = 2;
				value = 700 + (int) (Math.random() * 500);
			}

			if ((selectSignBlock.getTypeId() != 68) && (selectSignBlock.getTypeId() != 63)) {
				System.out.println("Autospawn:No select sign detected error");
				return;
			}

			CuboidClipboard ccb = autoSelectSign((Sign) selectSignBlock.getState(), player);

			if (ccb == null) {
				System.out.println("Autospawn:Failed to select clipboard");
				return;
			}

			Block pasteBlock = block.getRelative(bf, 0);
			ccb.rotate2D(rotate);
			ccb.paste(es, new Vector(pasteBlock.getX(), pasteBlock.getY() - 1, pasteBlock.getZ()), false);
			es.flushQueue();
			ccb.rotate2D(-rotate);


			es.setBlockChangeLimit(oldLimit);

			if (isAutoSpawn) {

				int shiftRight = 16;
				int shiftForward = 55;
				int shiftUp = 20;
				int shiftDown = 8;

				Block rightLimit = block.getRelative(bf2, shiftRight).getRelative(bf, shiftForward).getRelative(BlockFace.UP, shiftUp);
				Block leftLimit = block.getRelative(bf, 1).getRelative(BlockFace.DOWN, shiftDown);
				int rightX, rightY, rightZ;
				int leftX, leftY, leftZ;
				rightX = rightLimit.getX();
				rightY = rightLimit.getY();
				rightZ = rightLimit.getZ();
				leftX = leftLimit.getX();
				leftY = leftLimit.getY();
				leftZ = leftLimit.getZ();
				int startX, endX, startZ, endZ;
				if (rightX < leftX) {
					startX = rightX;
					endX = leftX;
				} else {
					startX = leftX;
					endX = rightX;
				}
				if (rightZ < leftZ) {
					startZ = rightZ;
					endZ = leftZ;
				} else {
					startZ = leftZ;
					endZ = rightZ;
				}

				for (int x = startX; x <= endX; x++) {
					for (int y = leftY; y <= rightY; y++) {
						for (int z = startZ; z <= endZ; z++) {
							if (player.getWorld().getBlockAt(x, y, z).getTypeId() == 68) {
								Block shipSignBlock = player.getWorld().getBlockAt(x, y, z);
								Sign shipSign = (Sign) shipSignBlock.getState();
								String signLine0 = shipSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
								CraftType craftType = CraftType.getCraftType(signLine0);
								if (craftType != null) {
									String name = shipSign.getLine(1);// .replaceAll("§.", "");

									if (name.trim().equals("")) {
										name = null;
									}

									int shipx = shipSignBlock.getX();
									int shipy = shipSignBlock.getY();
									int shipz = shipSignBlock.getZ();

									int direction = shipSignBlock.getData();

									// get the block the sign is attached to
									shipx = shipx + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
									shipz = shipz + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

									float dr = 0;

									switch (shipSignBlock.getData()) {
										case (byte) 0x2:// n
											dr = 180;
											break;
										case (byte) 0x3:// s
											dr = 0;
											break;
										case (byte) 0x4:// w
											dr = 90;
											break;
										case (byte) 0x5:// e
											dr = 270;
											break;
									}
									Craft theCraft = NavyCraft.instance.createCraft(player, craftType, shipx, shipy, shipz, name, dr, shipSignBlock, true);

									if (theCraft == null) {
										System.out.println("Autospawner: Failed to create craft.");
										return;
									}

									CraftMover cm = new CraftMover(theCraft, plugin);
									cm.structureUpdate(null, false);

									if (isAutoSpawn) {
										theCraft.isAutoCraft = true;
										theCraft.speedChange(null, true);
										theCraft.speedChange(null, true);
										theCraft.speedChange(null, true);
										theCraft.speedChange(null, true);

										double randomNum = Math.random();
										if (randomNum >= .5) {
											theCraft.gearChange(null, true);
										}

										theCraft.sinkValue = value;
										theCraft.routeID = routeID;
										if (spawnType == 0) {
											System.out.println("Maru spawned route=" + routeID);
										} else if (spawnType == 1) {
											System.out.println("T2 spawned route=" + routeID);
										} else {
											System.out.println("Victory Cargo spawned route=" + routeID);
										}

									}

									return;
								}
							}
						}
					}
				}
				System.out.println("No ship sign located!");
			}

		} catch (MaxChangedBlocksException e) {
			System.out.println("Max changed blocks error");
			return;
		}
	}

	public static CuboidClipboard autoSelectSign(Sign sign, Player player) {
		BlockFace bf;
		bf = null;
		Block block = sign.getBlock();
		// bf2 = null;
		switch (block.getData()) {
			case (byte) 0x8:// n
				bf = BlockFace.SOUTH;
				// bf2 = BlockFace.NORTH;
				break;
			case (byte) 0x0:// s
				bf = BlockFace.NORTH;
				// bf2 = BlockFace.SOUTH;
				break;
			case (byte) 0x4:// w
				bf = BlockFace.EAST;
				// bf2 = BlockFace.WEST;
				break;
			case (byte) 0xC:// e
				bf = BlockFace.WEST;
				// bf2 = BlockFace.EAST;
				break;
			default:
				break;
		}

		if (bf == null) {
			System.out.println("Sign error...check direction?");
			return null;
		}

		if (block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getTypeId() == 68) {
			String spawnName = sign.getLine(3).trim().toLowerCase();
			Sign sign2 = (Sign) block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getState();
			String lotStr = sign2.getLine(3).trim().toLowerCase();
			spawnName = spawnName.replaceAll(ChatColor.BLUE.toString(), "");
			lotStr = lotStr.replaceAll(ChatColor.BLUE.toString(), "");

			int lotType = 0;
			if (lotStr.equalsIgnoreCase("DD") || lotStr.equalsIgnoreCase("SHIP1")) {
				lotType = 1;
			} else if (lotStr.equalsIgnoreCase("SUB1") || lotStr.equalsIgnoreCase("SHIP2")) {
				lotType = 2;
			} else if (lotStr.equalsIgnoreCase("SUB2") || lotStr.equalsIgnoreCase("SHIP3")) {
				lotType = 3;
			} else if (lotStr.equalsIgnoreCase("CL") || lotStr.equalsIgnoreCase("SHIP4")) {
				lotType = 4;
			} else if (lotStr.equalsIgnoreCase("CA") || lotStr.equalsIgnoreCase("SHIP5")) {
				lotType = 5;
			} else if (lotStr.equalsIgnoreCase("HANGAR1")) {
				lotType = 6;
			} else if (lotStr.equalsIgnoreCase("HANGAR2")) {
				lotType = 7;
			} else if (lotStr.equalsIgnoreCase("TANK1")) {
				lotType = 8;
			} else if (lotStr.equalsIgnoreCase("TANK2")) {
				lotType = 9;
			} else {
				System.out.println("Sign error...lot type");
				return null;
			}

			wep = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
			if (wep == null) {
				System.out.println("WorldEdit error");
				return null;
			}

			Location loc;
			int sizeX, sizeY, sizeZ, originX, originY, originZ, offsetX, offsetY, offsetZ;
			if (lotType == 1) {
				loc = block.getRelative(bf, 28).getLocation();
				sizeX = 13;
				sizeY = 28;
				sizeZ = 28;
				originX = 0;
				originY = -8;
				originZ = 0;
				offsetX = 0;
				offsetY = -7;
				offsetZ = -29;
			} else if (lotType == 2) {
				loc = block.getRelative(bf, 43).getLocation();
				sizeX = 9;
				sizeY = 28;
				sizeZ = 43;
				originX = 0;
				originY = -8;
				originZ = 0;
				offsetX = 0;
				offsetY = -7;
				offsetZ = -44;
			} else if (lotType == 3) {
				loc = block.getRelative(bf, 70).getLocation();
				sizeX = 11;
				sizeY = 28;
				sizeZ = 70;
				originX = 0;
				originY = -8;
				originZ = 0;
				offsetX = 0;
				offsetY = -7;
				offsetZ = -71;
			} else if (lotType == 4) {
				loc = block.getRelative(bf, 55).getLocation();
				sizeX = 17;
				sizeY = 28;
				sizeZ = 55;
				originX = 0;
				originY = -8;
				originZ = 0;
				offsetX = 0;
				offsetY = -7;
				offsetZ = -56;
			} else if (lotType == 5) {
				loc = block.getRelative(bf, 98).getLocation();
				sizeX = 17;
				sizeY = 28;
				sizeZ = 98;
				originX = 0;
				originY = -8;
				originZ = 0;
				offsetX = 0;
				offsetY = -7;
				offsetZ = -99;
			} else if (lotType == 6) {
				loc = block.getRelative(bf, 17).getLocation();
				sizeX = 17;
				sizeY = 7;
				sizeZ = 19;
				originX = 0;
				originY = -1;
				originZ = -18;
				offsetX = -17;
				offsetY = 0;
				offsetZ = -20;
			} else if (lotType == 7) {
				loc = block.getRelative(bf, 25).getLocation();
				sizeX = 25;
				sizeY = 7;
				sizeZ = 32;
				originX = 0;
				originY = -1;
				originZ = -31;
				offsetX = -25;
				offsetY = 0;
				offsetZ = -33;
			} else if (lotType == 8) {
				loc = block.getRelative(bf, 12).getLocation();
				sizeX = 12;
				sizeY = 7;
				sizeZ = 19;
				originX = 0;
				originY = -1;
				originZ = -18;
				offsetX = -12;
				offsetY = 0;
				offsetZ = -20;
			} else if (lotType == 9) {
				loc = block.getRelative(bf, 12).getLocation();
				sizeX = 25;
				sizeY = 9;
				sizeZ = 33;
				originX = 0;
				originY = -1;
				originZ = -18;
				offsetX = -12;
				offsetY = 0;
				offsetZ = -20;
			} else

			{
				System.out.println("Sign Error: Invalid Lot");
				return null;
			}

			CuboidRegion region = new CuboidRegion(new Vector(loc.getBlockX() + originX, loc.getBlockY() + originY, loc.getBlockZ() + originZ), new Vector(loc.getBlockX() + originX + sizeX, loc.getBlockY() + originY + sizeY, loc.getBlockZ() + originZ + sizeZ));

			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
			clipboard.setOrigin(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			WorldData worldData = LegacyWorldData.getInstance();
			ClipboardHolder ch = new ClipboardHolder(clipboard, worldData);
			wep.getSession(player).setClipboard(ch);

			CuboidClipboard cclipb = new CuboidClipboard(new Vector(sizeX, sizeY, sizeZ), new Vector(loc.getBlockX() + originX, loc.getBlockY() + originY, loc.getBlockZ() + originZ), new Vector(offsetX, offsetY, offsetZ));
			return cclipb;


		} else {
			System.out.println(ChatColor.RED + "Sign Error: Check Second Sign?");
			return null;
		}
	}

	public static void loadDDSigns() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1567; x+=14 )
		// for( int x=16; x<=1286; x+=14 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-852; z-=37 )
			// for( int z=-18; z>=-462; z-=37 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x, 63, z + 1);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0);
					String sign2Line2 = selectSign2.getLine(2);

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerDDSigns.containsKey(playerName)) {
							NavyCraft.playerDDSigns.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerDDSigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerDDSigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findDDOpen() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1567; x+=14 )
		// for( int x=16; x<=1286; x+=14 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-852; z-=37 )
			// for( int z=-18; z>=-462; z-=37 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadSUB1Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1421; x+=10 )
		// for( int x=16; x<=1296; x+=10 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-356; z<=-148; z+=52 )
			// for( int z=33; z<=241; z+=52 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x, 63, z + 1);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0);
					String sign2Line2 = selectSign2.getLine(2);

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerSUB1Signs.containsKey(playerName)) {
							NavyCraft.playerSUB1Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSUB1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSUB1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSUB1Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1421; x+=10 )
		// for( int x=16; x<=1296; x+=10 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-356; z<=-148; z+=52 )
			// for( int z=33; z<=241; z+=52 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadSUB2Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=549; x>=21; x-=12 )
		// for( int x=-35; x>=-1091; x-=12 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-329; z<=-92; z+=79 )
			// for( int z=60; z<=297; z+=79 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x, 63, z + 1);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0);
					String sign2Line2 = selectSign2.getLine(2);

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerSUB2Signs.containsKey(playerName)) {
							NavyCraft.playerSUB2Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSUB2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSUB2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSUB2Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=549; x>=21; x-=12 )
		// for( int x=-35; x>=-1091; x-=12 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship3_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {
			// for( int z=-329; z<=-92; z+=79 )
			// for( int z=60; z<=297; z+=79 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadCLSigns() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=543; x>=21; x-=18 )
		// for( int x=-41; x>=-1085; x-=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-600; z-=64 )
			// for( int z=-18; z>=-210; z-=64 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x, 63, z + 1);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerCLSigns.containsKey(playerName)) {
							NavyCraft.playerCLSigns.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerCLSigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerCLSigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findCLOpen() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=543; x>=21; x-=18 )
		// for( int x=-41; x>=-1085; x-=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship4_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-600; z-=64 )
			// for( int z=-18; z>=-210; z-=64 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadCASigns() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=1404; x>=656; x-=22 )
		// for( int x=1270; x>=16; x-=22 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=37; z<=142; z+=105 )
			// for( int z=349; z<=454; z+=105 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x, 63, z + 1);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerCASigns.containsKey(playerName)) {
							NavyCraft.playerCASigns.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerCASigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerCASigns.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findCAOpen() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=1404; x>=656; x-=22 )
		// for( int x=1270; x>=16; x-=22 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Ship5_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {
			// for( int z=37; z<=142; z+=105 )
			// for( int z=349; z<=454; z+=105 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x, 63, z + 1).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadHANGAR1Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=553; x>=-137; x-=23 )
		// for( int x=-31; x>=-1067; x-=23 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerHANGAR1Signs.containsKey(playerName)) {
							NavyCraft.playerHANGAR1Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerHANGAR1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerHANGAR1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findHANGAR1Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=553; x>=-137; x-=23 )
		// for( int x=-31; x>=-1067; x-=23 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadHANGAR2Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=2000; x>=2001; x-=1 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=2000; z>=2001; z-=1 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 64, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 64, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 63, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerHANGAR2Signs.containsKey(playerName)) {
							NavyCraft.playerHANGAR2Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerHANGAR2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerHANGAR2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findHANGAR2Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=2000; x>=2001; x-=1 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Hangar2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=2000; z>=2001; z-=1 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadTANK1Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=602; x<=908; x+=18 )
		// for( int x=22; x<=832; x+=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-953; z>=-1385; z-=24 )
			// for( int z=-500; z>=-932; z-=24 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerTANK1Signs.containsKey(playerName)) {
							NavyCraft.playerTANK1Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerTANK1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerTANK1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findTANK1Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=602; x<=908; x+=18 )
		// for( int x=22; x<=832; x+=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank1_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-953; z>=-1385; z-=24 )
			// for( int z=-500; z>=-932; z-=24 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}
	public static void loadTANK2Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=602; x<=908; x+=18 )
		// for( int x=22; x<=832; x+=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-953; z>=-1385; z-=24 )
			// for( int z=-500; z>=-932; z-=24 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 64, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					Sign selectSign2 = (Sign) selectSignBlock2.getState();
					String signLine0 = selectSign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
					String sign2Line2 = selectSign2.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

					if (signLine0.equalsIgnoreCase("*select*")) {
						String playerName = selectSign.getLine(1);
						String playerName2 = selectSign.getLine(2);

						if ((playerName2 != null) && !playerName2.isEmpty()) {
							playerName = playerName + playerName2;
						}

						if (playerName == null) {
							continue;
						}

						int idNum = -1;
						try {
							idNum = Integer.parseInt(sign2Line2);
						} catch (NumberFormatException nfe) {
							continue;
						}
						if (idNum == -1) {
							continue;
						}

						if (!NavyCraft.playerTANK2Signs.containsKey(playerName)) {
							NavyCraft.playerTANK2Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerTANK2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerTANK2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findTANK2Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=602; x<=908; x+=18 )
		// for( int x=22; x<=832; x+=18 )
		int startX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_StartX"));
		int endX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_EndX"));
		int widthX = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_WidthX"));
		int startZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_StartZ"));
		int endZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_EndZ"));
		int widthZ = Integer.parseInt(NavyCraft.instance.ConfigSetting("Tank2_WidthZ"));
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-953; z>=-1385; z-=24 )
			// for( int z=-500; z>=-932; z-=24 )
			for (int z = startZ; z <= endZ; z += widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Sign selectSign = (Sign) selectSignBlock.getState();
					String signLine0 = selectSign.getLine(0);

					if (signLine0.equalsIgnoreCase("*claim*")) { return selectSignBlock; }
				}
			}
		}
		return null;
	}

	public static void loadShipyard() {
		for (String s : NavyCraft.playerDDSigns.keySet()) {
			NavyCraft.playerDDSigns.get(s).clear();
		}
		NavyCraft.playerDDSigns.clear();
		for (String s : NavyCraft.playerSUB1Signs.keySet()) {
			NavyCraft.playerSUB1Signs.get(s).clear();
		}
		NavyCraft.playerSUB1Signs.clear();
		for (String s : NavyCraft.playerSUB2Signs.keySet()) {
			NavyCraft.playerSUB2Signs.get(s).clear();
		}
		NavyCraft.playerSUB2Signs.clear();
		for (String s : NavyCraft.playerCLSigns.keySet()) {
			NavyCraft.playerCLSigns.get(s).clear();
		}
		NavyCraft.playerCLSigns.clear();
		for (String s : NavyCraft.playerCASigns.keySet()) {
			NavyCraft.playerCASigns.get(s).clear();
		}
		NavyCraft.playerCASigns.clear();
		for (String s : NavyCraft.playerHANGAR1Signs.keySet()) {
			NavyCraft.playerHANGAR1Signs.get(s).clear();
		}
		NavyCraft.playerHANGAR1Signs.clear();
		for (String s : NavyCraft.playerHANGAR2Signs.keySet()) {
			NavyCraft.playerHANGAR2Signs.get(s).clear();
		}
		NavyCraft.playerHANGAR2Signs.clear();
		for (String s : NavyCraft.playerTANK1Signs.keySet()) {
			NavyCraft.playerTANK1Signs.get(s).clear();
		}
		NavyCraft.playerTANK1Signs.clear();
		loadDDSigns();
		loadSUB1Signs();
		loadSUB2Signs();
		loadCLSigns();
		loadCASigns();
		loadHANGAR1Signs();
		loadHANGAR2Signs();
		loadTANK1Signs();
	}
	
	public static void loadRewards(String player) {
		NavyCraft.playerDDRewards.clear();
		NavyCraft.playerSUB1Rewards.clear();
		NavyCraft.playerSUB2Rewards.clear();
		NavyCraft.playerCLRewards.clear();
		NavyCraft.playerCARewards.clear();
		NavyCraft.playerHANGAR1Rewards.clear();
		NavyCraft.playerHANGAR2Rewards.clear();
		NavyCraft.playerTANK1Rewards.clear();
		NavyCraft.playerTANK2Rewards.clear();

		String worldName = "";
		if(plugin.ConfigSetting("EnabledWorlds") != "null") {
			String[] worlds = NavyCraft.instance.ConfigSetting("EnabledWorlds").split(",");
			worldName = worlds[0];
		}else{
			worldName = plugin.getServer().getPlayer(player).getWorld().getName();
		}
		
		String worldName1 = "";
		if(plugin.ConfigSetting("BattleWorlds") != "null") {
			String[] worlds = NavyCraft.instance.ConfigSetting("BattleWorlds").split(",");
			worldName1 = worlds[0];
		}else{
			worldName1 = plugin.getServer().getPlayer(player).getWorld().getName();
		}
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if( pex==null )
			return;
		
		for(String s:PermissionsEx.getUser(player).getPermissions(worldName1)) {
			if( s.contains("navycraft") ) {
				if( s.contains("ship1") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerDDRewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s + " " + s.split(".").length);
					}
				}else if( s.contains("ship2") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSUB1Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship3") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSUB2Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship4") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerCLRewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship5") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerCARewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("hangar1") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerHANGAR1Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("hangar2") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerHANGAR2Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("tank1") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerTANK1Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("tank2") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerTANK2Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}
			}
		}


		NavyCraft.loadRewardsFile();

	}
	
	public static Sign findSign(String player, int id) {
		Sign foundSign = null;
		if (NavyCraft.playerDDSigns.containsKey(player)) {
			for (Sign s : NavyCraft.playerDDSigns.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSUB1Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSUB1Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSUB2Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSUB2Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerCLSigns.containsKey(player)) {
			for (Sign s : NavyCraft.playerCLSigns.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerCASigns.containsKey(player)) {
			for (Sign s : NavyCraft.playerCASigns.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerHANGAR1Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerHANGAR1Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerHANGAR2Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerHANGAR2Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerTANK1Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerTANK1Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		return foundSign;
	}

	public static int maxId(Player player) {
		int foundHighest = -1;
		if (NavyCraft.playerDDSigns.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerDDSigns.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSUB1Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSUB1Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSUB2Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSUB2Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerCLSigns.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerCLSigns.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerCASigns.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerCASigns.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerHANGAR1Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerHANGAR1Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerHANGAR2Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerHANGAR2Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerTANK1Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerTANK1Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		return foundHighest;
	}


	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDispense(final BlockDispenseEvent event) {
		if (!event.isCancelled()) {
			if (PermissionInterface.CheckEnabledWorld(event.getBlock().getLocation()) && (event.getItem().getType() == Material.EMERALD)) {
				event.setCancelled(true);
			}

		}
		
		if(!event.isCancelled())
			AimCannonPlayerListener.onBlockDispense(event);
	}
	public static void getRank(Player player) {
		int exp = 0;
		String worldName = player.getWorld().getName();
		
		NavyCraft.loadExperience();
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if( pex==null )
			return;
		
		int rankExp=0;
		for(String s:PermissionsEx.getUser(player).getPermissions(worldName)) {
			if( s.contains("navycraft") ) {
				if( s.contains("exp") ) {
					String[] split = s.split("\\.");
					try {
						rankExp = Integer.parseInt(split[2]);	
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}
			}
		}
		
		List<String> groupNames = PermissionsEx.getUser(player).getParentIdentifiers("navycraft");
		for( String s : groupNames ) {
			if( PermissionsEx.getPermissionManager().getGroup(s).getRankLadder().equalsIgnoreCase("navycraft") ) {
				if (NavyCraft.playerExp.containsKey(player.getName())) {
					exp = NavyCraft.playerExp.get(player.getName());
				}
				player.sendMessage(ChatColor.GRAY + "Your rank is " + ChatColor.WHITE + s.toUpperCase()
						+ ChatColor.GRAY + " and you have " + ChatColor.WHITE + exp + "/" + rankExp
						+ ChatColor.GRAY + " rank points.");
				
				if( exp >= rankExp )
				{
					CraftMover.checkRankWorld(player, exp, player.getWorld());
				}
				return;
			    }
		    }
	    }
	
	//Need to add back battles support.
	
	public static void rewardExpPlayer(int newExp, Player player) {
		if (NavyCraft.playerExp.containsKey(player.getName())) {
			newExp = NavyCraft.playerExp.get(player.getName()) + newExp;
			NavyCraft.playerExp.put(player.getName(), newExp);
		} else {
			NavyCraft.playerExp.put(player.getName(), newExp);
		}
		
		player.sendMessage(ChatColor.GRAY + "You now have " + ChatColor.WHITE + newExp + ChatColor.GRAY + " rank points.");
			
		CraftMover.checkRankWorld(player, newExp, player.getWorld());
		NavyCraft.saveExperience();
	}
	
	public static void rewardExpCraft(int newExp, Craft craft) {
		int playerNewExp = newExp;
		for (String s : craft.crewNames) {
			Player p = plugin.getServer().getPlayer(s);
			if (p != null) {
				playerNewExp = newExp;
				if (NavyCraft.playerExp.containsKey(p.getName())) {
					playerNewExp = NavyCraft.playerExp.get(p.getName()) + newExp;
					NavyCraft.playerExp.put(p.getName(), playerNewExp);
				} else {
					NavyCraft.playerExp.put(p.getName(), playerNewExp);
				}
				p.sendMessage(ChatColor.GRAY + "You now have " + ChatColor.WHITE + playerNewExp + ChatColor.GRAY + " rank points.");
				CraftMover.checkRankWorld(p, playerNewExp, craft.world);
			}
			
		}
		NavyCraft.saveExperience();
		
		
		
	}
	
	}
