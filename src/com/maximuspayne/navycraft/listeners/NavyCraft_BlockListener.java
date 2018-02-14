package com.maximuspayne.navycraft.listeners;

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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.maximuspayne.aimcannon.AimCannonPlayerListener;
import com.maximuspayne.navycraft.NavyCraft;
import com.maximuspayne.navycraft.Periscope;
import com.maximuspayne.navycraft.blocks.DataBlock;
import com.maximuspayne.navycraft.craft.Craft;
import com.maximuspayne.navycraft.craft.CraftMover;
import com.maximuspayne.navycraft.craft.CraftType;
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
import ru.tehkode.permissions.exceptions.RankingException;

@SuppressWarnings({ "deprecation", "unused" })
public class NavyCraft_BlockListener implements Listener {
	public static Craft updatedCraft = null;
	private static NavyCraft plugin;
	public static PermissionsEx pex;
	public static WorldEditPlugin wep;
	public static WorldGuardPlugin wgp;
	public static CraftMover cm;
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
		int lotType = 0; /// 1=SHIP1, 2=SHIP2, 3=SHIP3, 4=SHIP4, 5=SHIP5, 6=hangar1, 7=hangar2 8=tank1 9=tank2

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
				player.sendMessage(ChatColor.DARK_RED + "Sign Error: Check Direction?");
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
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: No Type");
					return;
				}

				int rankReq = -1;
				try {
					rankReq = Integer.parseInt(rankStr);
				} catch (NumberFormatException nfe) {
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invaild Rank Number");
					return;
				}

				if ((rankReq < 1) || (rankReq > 10)) {
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invalid Rank Requirement");
					return;
				}

				if (lotStr.equalsIgnoreCase("SHIP1")) {
					lotType = 1;
				} else if (lotStr.equalsIgnoreCase("SHIP2")) {
					lotType = 2;
				} else if (lotStr.equalsIgnoreCase("SHIP3")) {
					lotType = 3;
				} else if (lotStr.equalsIgnoreCase("SHIP4")) {
					lotType = 4;
				} else if (lotStr.equalsIgnoreCase("SHIP5")) {
					lotType = 5;
				} else if (lotStr.equalsIgnoreCase("HANGAR1")) {
					lotType = 6;
				} else if (lotStr.equalsIgnoreCase("HANGAR2")) {
					lotType = 7;
				} else if (lotStr.equalsIgnoreCase("TANK1")) {
					lotType = 8;
				} else if (lotStr.equalsIgnoreCase("TANK2")) {
					lotType = 9;
				} else if (lotStr.equalsIgnoreCase("MAP1")) {
					lotType = 10;
				} else if (lotStr.equalsIgnoreCase("MAP2")) {
					lotType = 11;
				} else if (lotStr.equalsIgnoreCase("MAP3")) {
					lotType = 12;
				} else if (lotStr.equalsIgnoreCase("MAP4")) {
					lotType = 13;
				} else if (lotStr.equalsIgnoreCase("MAP5")) {
					lotType = 14;
				} else {
					player.sendMessage(ChatColor.RED + "Sign error: lot type");
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
				} else if (lotType == 2) {
					loc = block.getRelative(bf, 43).getLocation();
					sizeX = 9;
					sizeY = 28;
					sizeZ = 43;
					originX = 0;
					originY = -8;
					originZ = 0;
				} else if (lotType == 3) {
					loc = block.getRelative(bf, 70).getLocation();
					sizeX = 11;
					sizeY = 28;
					sizeZ = 70;
					originX = 0;
					originY = -8;
					originZ = 0;
				} else if (lotType == 4) {
					loc = block.getRelative(bf, 55).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 55;
					originX = 0;
					originY = -8;
					originZ = 0;
				} else if (lotType == 5) {
					loc = block.getRelative(bf, 98).getLocation();
					sizeX = 17;
					sizeY = 28;
					sizeZ = 98;
					originX = 0;
					originY = -8;
					originZ = 0;
				} else if (lotType == 6) {
					loc = block.getRelative(bf, 17).getLocation();
					sizeX = 17;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
				} else if (lotType == 7) {
					loc = block.getRelative(bf, 25).getLocation();
					sizeX = 25;
					sizeY = 7;
					sizeZ = 32;
					originX = 0;
					originY = -1;
					originZ = -31;
				} else if (lotType == 8) {
					loc = block.getRelative(bf, 12).getLocation();
					sizeX = 12;
					sizeY = 7;
					sizeZ = 19;
					originX = 0;
					originY = -1;
					originZ = -18;
				} else if (lotType == 9) {
					loc = block.getRelative(bf, 27).getLocation();
					sizeX = 27;
					sizeY = 9;
					sizeZ = 33;
					originX = 0;
					originY = -1;
					originZ = -32;
				} else if (lotType == 10) {
					loc = block.getRelative(bf, 100).getLocation();
					sizeX = 100;
					sizeY = 255;
					sizeZ = 100;
					originX = 0;
					originY = -63;
					originZ = -99;
				} else if (lotType == 11) {
					loc = block.getRelative(bf, 150).getLocation();
					sizeX = 150;
					sizeY = 255;
					sizeZ = 150;
					originX = 0;
					originY = -63;
					originZ = -149;
				} else if (lotType == 12) {
					loc = block.getRelative(bf, 200).getLocation();
					sizeX = 200;
					sizeY = 255;
					sizeZ = 200;
					originX = 0;
					originY = -63;
					originZ = -199;
				} else if (lotType == 13) {
					loc = block.getRelative(bf, 250).getLocation();
					sizeX = 250;
					sizeY = 255;
					sizeZ = 250;
					originX = 0;
					originY = -63;
					originZ = -249;
				} else if (lotType == 14) {
					loc = block.getRelative(bf, 500).getLocation();
					sizeX = 500;
					sizeY = 255;
					sizeZ = 500;
					originX = 0;
					originY = -63;
					originZ = -499;
				} else

				{
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invalid Lot");
					return;
				}

				CuboidRegion region = new CuboidRegion(new Vector(loc.getBlockX() + originX, loc.getBlockY() + originY, loc.getBlockZ() + originZ), new Vector((loc.getBlockX() + originX + sizeX) - 1, (loc.getBlockY() + originY + sizeY) - 1, (loc.getBlockZ() + originZ + sizeZ) - 1));

				BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
				try {

					if ((lotType >= 6) && (lotType <= 14)) {
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
				player.sendMessage(ChatColor.GREEN + "Selected vehicle: " + ChatColor.WHITE + spawnName.toUpperCase());

			} else {
				player.sendMessage(ChatColor.DARK_RED + "Sign Error: Check Second Sign?");
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
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: Check Direction?");
					return;
				}
			
				if (block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getTypeId() == 68) {
					Sign sign2 = (Sign) block.getRelative(BlockFace.DOWN, 1).getRelative(bf, -1).getState();
					String lotStr = sign2.getLine(3).trim().toLowerCase();
					lotStr = lotStr.replaceAll(ChatColor.BLUE.toString(), "");
			
					if (lotStr.equalsIgnoreCase("SHIP1")) {
						lotType = 1;
					} else if (lotStr.equalsIgnoreCase("SHIP2")) {
						lotType = 2;
					} else if (lotStr.equalsIgnoreCase("SHIP3")) {
						lotType = 3;
					} else if (lotStr.equalsIgnoreCase("SHIP4")) {
						lotType = 4;
					} else if (lotStr.equalsIgnoreCase("SHIP5")) {
						lotType = 5;
					} else if (lotStr.equalsIgnoreCase("HANGAR1")) {
						lotType = 6;
					} else if (lotStr.equalsIgnoreCase("HANGAR2")) {
						lotType = 7;
					} else if (lotStr.equalsIgnoreCase("TANK1")) {
						lotType = 8;
					} else if (lotStr.equalsIgnoreCase("TANK2")) {
						lotType = 9;
					} else if (lotStr.equalsIgnoreCase("MAP1")) {
						lotType = 10;
					} else if (lotStr.equalsIgnoreCase("MAP2")) {
						lotType = 11;
					} else if (lotStr.equalsIgnoreCase("MAP3")) {
						lotType = 12;
					} else if (lotStr.equalsIgnoreCase("MAP4")) {
						lotType = 13;
					} else if (lotStr.equalsIgnoreCase("MAP5")) {
						lotType = 14;
					} else {
						player.sendMessage(ChatColor.DARK_RED + "Sign Error: Lot Type");
						return;
					}

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
			
						int numSHIP1s = 0;
						int numRewSHIP1s = 1;
						if (NavyCraft.playerSHIP1Signs.containsKey(player.getName())) {
							numSHIP1s = NavyCraft.playerSHIP1Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerSHIP1Rewards.containsKey(player.getName())) {
							numRewSHIP1s = NavyCraft.playerSHIP1Rewards.get(player.getName());
						}
						if (numSHIP1s >= numRewSHIP1s) {
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
			
						int numSHIP2s = 0;
						int numRewSHIP2s = 0;
						if (NavyCraft.playerSHIP2Signs.containsKey(player.getName())) {
							numSHIP2s = NavyCraft.playerSHIP2Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerSHIP2Rewards.containsKey(player.getName())) {
							numRewSHIP2s = NavyCraft.playerSHIP2Rewards.get(player.getName());
						}
						if (numSHIP2s >= numRewSHIP2s) {
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
			
						int numSHIP3s = 0;
						int numRewSHIP3s = 0;
						if (NavyCraft.playerSHIP3Signs.containsKey(player.getName())) {
							numSHIP3s = NavyCraft.playerSHIP3Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerSHIP3Rewards.containsKey(player.getName())) {
							numRewSHIP3s = NavyCraft.playerSHIP3Rewards.get(player.getName());
						}
						if (numSHIP3s >= numRewSHIP3s) {
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
			
						int numSHIP4s = 0;
						int numRewSHIP4s = 0;
						if (NavyCraft.playerSHIP4Signs.containsKey(player.getName())) {
							numSHIP4s = NavyCraft.playerSHIP4Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerSHIP4Rewards.containsKey(player.getName())) {
							numRewSHIP4s = NavyCraft.playerSHIP4Rewards.get(player.getName());
						}
						if (numSHIP4s >= numRewSHIP4s) {
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
			
						int numSHIP5s = 0;
						int numRewSHIP5s = 0;
						if (NavyCraft.playerSHIP5Signs.containsKey(player.getName())) {
							numSHIP5s = NavyCraft.playerSHIP5Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerSHIP5Rewards.containsKey(player.getName())) {
							numRewSHIP5s = NavyCraft.playerSHIP5Rewards.get(player.getName());
						}
						if (numSHIP5s >= numRewSHIP5s) {
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
					} else if (lotType == 9) {
						loc = block.getRelative(bf, 27).getLocation();
						sizeX = 27;
						sizeY = 9;
						sizeZ = 33;
						originX = 0;
						originY = -1;
						originZ = -32;
						offsetX = -27;
						offsetY = 0;
						offsetZ = -34;
			
						int numT2s = 0;
						int numRewT2s = 0;
						if (NavyCraft.playerTANK2Signs.containsKey(player.getName())) {
							numT2s = NavyCraft.playerTANK2Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerTANK2Rewards.containsKey(player.getName())) {
							numRewT2s = NavyCraft.playerTANK2Rewards.get(player.getName());
						}
						if (numT2s >= numRewT2s) {
							player.sendMessage("You have no TANK2 reward plots available.");
							return;
						}
					} else if (lotType == 10) {
						loc = block.getRelative(bf, 100).getLocation();
						sizeX = 100;
						sizeY = 255;
						sizeZ = 100;
						originX = 0;
						originY = -63;
						originZ = -99;
						offsetX = -100;
						offsetY = 0;
						offsetZ = -101;
			
						int numM1s = 0;
						int numRewM1s = 0;
						if (NavyCraft.playerMAP1Signs.containsKey(player.getName())) {
							numM1s = NavyCraft.playerMAP1Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerMAP1Rewards.containsKey(player.getName())) {
							numRewM1s = NavyCraft.playerMAP1Rewards.get(player.getName());
						}
						if (numM1s >= numRewM1s) {
							player.sendMessage("You have no MAP1 reward plots available.");
							return;
						}
					} else if (lotType == 11) {
						loc = block.getRelative(bf, 150).getLocation();
						sizeX = 150;
						sizeY = 255;
						sizeZ = 150;
						originX = 0;
						originY = -63;
						originZ = -149;
						offsetX = -150;
						offsetY = 0;
						offsetZ = -151;
			
						int numM2s = 0;
						int numRewM2s = 0;
						if (NavyCraft.playerMAP2Signs.containsKey(player.getName())) {
							numM2s = NavyCraft.playerMAP2Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerMAP2Rewards.containsKey(player.getName())) {
							numRewM2s = NavyCraft.playerMAP2Rewards.get(player.getName());
						}
						if (numM2s >= numRewM2s) {
							player.sendMessage("You have no MAP2 reward plots available.");
							return;
						}
					} else if (lotType == 12) {
						loc = block.getRelative(bf, 200).getLocation();
						sizeX = 200;
						sizeY = 255;
						sizeZ = 200;
						originX = 0;
						originY = -63;
						originZ = -199;
						offsetX = -200;
						offsetY = 0;
						offsetZ = -201;
			
						int numM3s = 0;
						int numRewM3s = 0;
						if (NavyCraft.playerMAP3Signs.containsKey(player.getName())) {
							numM3s = NavyCraft.playerMAP3Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerMAP3Rewards.containsKey(player.getName())) {
							numRewM3s = NavyCraft.playerMAP3Rewards.get(player.getName());
						}
						if (numM3s >= numRewM3s) {
							player.sendMessage("You have no MAP3 reward plots available.");
							return;
						}
					} else if (lotType == 13) {
						loc = block.getRelative(bf, 250).getLocation();
						sizeX = 250;
						sizeY = 255;
						sizeZ = 250;
						originX = 0;
						originY = -63;
						originZ = -249;
						offsetX = -250;
						offsetY = 0;
						offsetZ = -251;
			
						int numM4s = 0;
						int numRewM4s = 0;
						if (NavyCraft.playerMAP4Signs.containsKey(player.getName())) {
							numM4s = NavyCraft.playerMAP4Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerMAP4Rewards.containsKey(player.getName())) {
							numRewM4s = NavyCraft.playerMAP4Rewards.get(player.getName());
						}
						if (numM4s >= numRewM4s) {
							player.sendMessage("You have no MAP4 reward plots available.");
							return;
						}
					} else if (lotType == 14) {
						loc = block.getRelative(bf, 500).getLocation();
						sizeX = 500;
						sizeY = 255;
						sizeZ = 500;
						originX = 0;
						originY = -63;
						originZ = -499;
						offsetX = -500;
						offsetY = 0;
						offsetZ = -501;
			
						int numM5s = 0;
						int numRewM5s = 0;
						if (NavyCraft.playerMAP5Signs.containsKey(player.getName())) {
							numM5s = NavyCraft.playerMAP5Signs.get(player.getName()).size();
						}
						if (NavyCraft.playerMAP5Rewards.containsKey(player.getName())) {
							numRewM5s = NavyCraft.playerMAP5Rewards.get(player.getName());
						}
						if (numM5s >= numRewM5s) {
							player.sendMessage("You have no MAP5 reward plots available.");
							return;
						}
					} else
			
					{
						player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invalid Lot");
						return;
					}
					originX = loc.getBlockX() + originX;
					originY = loc.getBlockY() + originY;
					originZ = loc.getBlockZ() + originZ;
			
					wgp = (WorldGuardPlugin)plugin.getServer().getPluginManager().getPlugin("WorldGuard");
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
			
						player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + lotStr.toUpperCase() + ChatColor.DARK_GRAY + "]" + ChatColor.GREEN + " Claimed!");
						loadShipyard();
					} else {
						player.sendMessage("World Guard error");
					}
			
				} else {
					player.sendMessage(ChatColor.DARK_RED + "Sign error: Check Second Sign?");
					return;
				}
		} else if (craftTypeName.equalsIgnoreCase("*battle*")) {
			{
				player.sendMessage(ChatColor.DARK_RED + "This sign is unfinished, Please discontinue use until the developer has had time to further develop this sign type.");
			}

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
				player.sendMessage(ChatColor.RED + "Go to the Shipyard and select a vehicle first.");
				return;
			}

			if (!checkSpawnerClear(player, block, bf, bf2)) {
				player.sendMessage(ChatColor.RED + "Vehicle in the way.");
				return;
			}

			boolean isMerchantSpawn = false;
			String freeString = sign.getLine(2).trim().toLowerCase();
			freeString = freeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (freeString.equalsIgnoreCase("merchant")) {
				isMerchantSpawn = true;
			}

			String typeString = sign.getLine(1).trim().toLowerCase();
			typeString = typeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!typeString.isEmpty() && !typeString.equalsIgnoreCase(Craft.playerClipboardsType.get(player)) && !typeString.equalsIgnoreCase(Craft.playerClipboardsLot.get(player))) {
				player.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.RED + ", you cannot spawn this type of vehicle here.");
				return;
			}

			int freeSpawnRankLimit = 0;
			String freeSpawnRankStr = sign.getLine(3).trim().toLowerCase();
			freeSpawnRankStr = freeSpawnRankStr.replaceAll(ChatColor.BLUE.toString(), "");
			if (!freeSpawnRankStr.isEmpty()) {
				try {
					freeSpawnRankLimit = Integer.parseInt(freeSpawnRankStr);
				} catch (NumberFormatException nfe) {
					player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invaild Rank Limit");
					return;
				}
			}

			if (freeSpawnRankLimit < 0) {
				player.sendMessage(ChatColor.DARK_RED + "Sign Error: Invaild Rank Limit");
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
				es.setBlockChangeLimit(100000);

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

				player.sendMessage(ChatColor.GREEN + "Spawned vehicle: " + ChatColor.WHITE + Craft.playerClipboardsType.get(player).toUpperCase());


				es.setBlockChangeLimit(oldLimit);

				int shiftRight = 0;
				int shiftForward = 0;
				int shiftUp = 0;
				int shiftDown = 0;
				{
					if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP1")) {
						shiftRight = 12;
						shiftForward = 28;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP2")) {
						shiftRight = 8;
						shiftForward = 43;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP3")) {
						shiftRight = 10;
						shiftForward = 70;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP4")) {
						shiftRight = 16;
						shiftForward = 55;
						shiftUp = 20;
						shiftDown = 8;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP5")) {
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
						shiftUp = 14;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK1")) {
						shiftRight = 11;
						shiftForward = 19;
						shiftUp = 7;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("TANK2")) {
						shiftRight = 25;
						shiftForward = 34;
						shiftUp = 9;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("MAP1")) {
						shiftRight = 25;
						shiftForward = 34;
						shiftUp = 9;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("MAP2")) {
						shiftRight = 25;
						shiftForward = 34;
						shiftUp = 9;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("MAP3")) {
						shiftRight = 25;
						shiftForward = 34;
						shiftUp = 9;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("MAP4")) {
						shiftRight = 25;
						shiftForward = 34;
						shiftUp = 9;
						shiftDown = 0;
					} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("MAP5")) {
						shiftRight = 25;
						shiftForward = 34;
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
										Craft theCraft = NavyCraft.instance.createCraft(player, craftType, shipx, shipy, shipz, name, dr, shipSignBlock);

										CraftMover cm = new CraftMover(theCraft, plugin);
										cm.structureUpdate(null, false);
										if (isMerchantSpawn) {
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
				player.sendMessage(ChatColor.GOLD + "You get off the AA-Gun.");
			}
			if (NavyCraft.flakGunnersList.contains(player)) {
				NavyCraft.flakGunnersList.remove(player);
				if (player.getInventory().contains(Material.BLAZE_ROD)) {
					player.getInventory().remove(Material.BLAZE_ROD);
				}
				player.sendMessage(ChatColor.GOLD + "You get off the Flak-Gun.");
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
							CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
							Location newLoc = new Location(playerCraft.world, p.scopeLoc.getBlockX() + 0.5, p.scopeLoc.getBlockY() + 0.5, p.scopeLoc.getBlockZ() + 0.5);
							newLoc.setYaw(player.getLocation().getYaw());
							player.teleport(newLoc);
							p.user = player;
						} else if (!p.destroyed && (p.scopeLoc != null)) {
							player.sendMessage(ChatColor.RED + "Raise Periscope First.");
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
						CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
							p.sendMessage(ChatColor.GREEN + "Surface the boat!");
						}
					}

					surfaceBellThread(sign.getBlock().getLocation());

				} else {
					player.sendMessage(ChatColor.GOLD + "Starting Electric Engines (READY TO DIVE)");
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
						CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
						CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
					CraftMover.playOtherSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.0f);
				} else {
					c.radarOn = false;
					player.sendMessage(ChatColor.RED + "Radar DEACTIVATED!");
					CraftMover.playOtherSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.5f, 1.0f);
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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				} else {
					c.sonarOn = false;
					player.sendMessage(ChatColor.RED + "Sonar DEACTIVATED!");
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
				} else {
					c.hfOn = false;
					player.sendMessage(ChatColor.RED + "High Frequency Sonar DEACTIVATED!");
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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

					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
				CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

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
			player.sendMessage(ChatColor.GOLD + "Manning Flak-Gun! Left Click with Blaze Rod to fire!");
			CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

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
			player.sendMessage(ChatColor.GOLD + "Manning AA-Gun! Left Click with Blaze Rod to fire!");
			CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);


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
				CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

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
				CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

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
					CraftMover.playOtherSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
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
						player.sendMessage(ChatColor.RED + "This vehicle will become abandoned in 2 minutes.");
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
				Craft theCraft = NavyCraft.instance.createCraft(player, craftType, x, y, z, name, dr, block);


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
					for (int i = 0; i < 8; i++) {
						sleep(200);
						if ((i % 2) == 0) {
							CraftMover.playOtherSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 1.2f);
						} else {
							CraftMover.playOtherSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 1);
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
					for (int i = 0; i < 2; i++) {
						sleep(300);
						CraftMover.playOtherSound(loc, Sound.BLOCK_NOTE_PLING, 1.0f, 2);
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
		if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP1")) {
			shiftRight = 12;
			shiftForward = 28;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP2")) {
			shiftRight = 8;
			shiftForward = 43;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP3")) {
			shiftRight = 10;
			shiftForward = 70;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP4")) {
			shiftRight = 16;
			shiftForward = 55;
			shiftUp = 20;
			shiftDown = 8;
		} else if (Craft.playerClipboardsLot.get(player).equalsIgnoreCase("SHIP5")) {
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
			shiftUp = 14;
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

	public static void loadSHIP1Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1567; x+=14 )
		// for( int x=16; x<=1286; x+=14 )
		int startX = 601;
		int endX = 1567;
		int widthX = 14;
		int startZ = -408;
		int endZ = -852;
		int widthZ = 37;
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-852; z-=37 )
			// for( int z=-18; z>=-462; z-=37 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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

						if (!NavyCraft.playerSHIP1Signs.containsKey(playerName)) {
							NavyCraft.playerSHIP1Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSHIP1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSHIP1Signs.get(playerName).add(selectSign);
						NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSHIP1Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1567; x+=14 )
		// for( int x=16; x<=1286; x+=14 )
		int startX = 601;
		int endX = 1567;
		int widthX = 14;
		int startZ = -408;
		int endZ = -852;
		int widthZ = 37;
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=-408; z>=-852; z-=37 )
			// for( int z=-18; z>=-462; z-=37 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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

	public static void loadSHIP2Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1421; x+=10 )
		// for( int x=16; x<=1296; x+=10 )
		int startX = 601;
		int endX = 1421;
		int widthX = 10;
		int startZ = -356;
		int endZ = -148;
		int widthZ = 52;
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

						if (!NavyCraft.playerSHIP2Signs.containsKey(playerName)) {
							NavyCraft.playerSHIP2Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSHIP2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSHIP2Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSHIP2Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=601; x<=1421; x+=10 )
		// for( int x=16; x<=1296; x+=10 )
		int startX = 601;
		int endX = 1421;
		int widthX = 10;
		int startZ = -356;
		int endZ = -148;
		int widthZ = 52;
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

	public static void loadSHIP3Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=549; x>=21; x-=12 )
		// for( int x=-35; x>=-1091; x-=12 )
		int startX = 549;
		int endX = 21;
		int widthX = 12;
		int startZ = -329;
		int endZ = -92;
		int widthZ = 79;
		for (int x = startX; x >= endX; x -= widthX) {

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

						if (!NavyCraft.playerSHIP3Signs.containsKey(playerName)) {
							NavyCraft.playerSHIP3Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSHIP3Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSHIP3Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSHIP3Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=549; x>=21; x-=12 )
		// for( int x=-35; x>=-1091; x-=12 )
		int startX = 549;
		int endX = 21;
		int widthX = 12;
		int startZ = -329;
		int endZ = -92;
		int widthZ = 79;
		for (int x = startX; x >= endX; x -= widthX) {
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

	public static void loadSHIP4Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=543; x>=21; x-=18 )
		// for( int x=-41; x>=-1085; x-=18 )
		int startX = 543;
		int endX = 21;
		int widthX = 18;
		int startZ = -408;
		int endZ = -600;
		int widthZ = 64;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-408; z>=-600; z-=64 )
			// for( int z=-18; z>=-210; z-=64 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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

						if (!NavyCraft.playerSHIP4Signs.containsKey(playerName)) {
							NavyCraft.playerSHIP4Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSHIP4Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSHIP4Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSHIP4Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=543; x>=21; x-=18 )
		// for( int x=-41; x>=-1085; x-=18 )
		int startX = 543;
		int endX = 21;
		int widthX = 18;
		int startZ = -408;
		int endZ = -600;
		int widthZ = 64;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-408; z>=-600; z-=64 )
			// for( int z=-18; z>=-210; z-=64 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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

	public static void loadSHIP5Signs() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=1404; x>=656; x-=22 )
		// for( int x=1270; x>=16; x-=22 )
		int startX = 656;
		int endX = 1426;
		int widthX = 18;
		int startZ = 142;
		int endZ = 37;
		int widthZ = 105;
		for (int x = startX; x <= endX; x += widthX) {

			// for( int z=37; z<=142; z+=105 )
			// for( int z=349; z<=454; z+=105 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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

						if (!NavyCraft.playerSHIP5Signs.containsKey(playerName)) {
							NavyCraft.playerSHIP5Signs.put(playerName, new ArrayList<Sign>());
							NavyCraft.playerSHIP5Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						} else {
							NavyCraft.playerSHIP5Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
						}

					}
				}
			}
		}
	}

	public static Block findSHIP5Open() {
		World syworld = plugin.getServer().getWorld("shipyard");
		// for( int x=1404; x>=656; x-=22 )
		// for( int x=1270; x>=16; x-=22 )
		int startX = 656;
		int endX = 1426;
		int widthX = 18;
		int startZ = 142;
		int endZ = 37;
		int widthZ = 105;
		for (int x = startX; x <= endX; x += widthX) {
			// for( int z=37; z<=142; z+=105 )
			// for( int z=349; z<=454; z+=105 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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
		int startX = 553;
		int endX = -137;
		int widthX = 23;
		int startZ = -766;
		int endZ = -1191;
		int widthZ = 25;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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
		int startX = 553;
		int endX = -137;
		int widthX = 23;
		int startZ = -766;
		int endZ = -1191;
		int widthZ = 25;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 64, z);
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
		// for( int x=553; x>=-137; x-=23 )
		// for( int x=-31; x>=-1067; x-=23 )
		int startX = -99;
		int endX = -1177;
		int widthX = 49;
		int startZ = 67;
		int endZ = -117;
		int widthZ = 46;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
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
		// for( int x=553; x>=-137; x-=23 )
		// for( int x=-31; x>=-1067; x-=23 )
		int startX = -99;
		int endX = -1177;
		int widthX = 49;
		int startZ = 67;
		int endZ = -117;
		int widthZ = 46;
		for (int x = startX; x >= endX; x -= widthX) {

			// for( int z=-766; z>=-1191; z-=25 )
			// for( int z=-278; z>=-828; z-=25 )
			for (int z = startZ; z >= endZ; z -= widthZ) {
				if ((syworld.getBlockAt(x, 65, z).getTypeId() == 63) && (syworld.getBlockAt(x + 1, 64, z).getTypeId() == 68)) {
					Block selectSignBlock = syworld.getBlockAt(x, 65, z);
					Block selectSignBlock2 = syworld.getBlockAt(x + 1, 64, z);
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
		int startX = 602;
		int endX = 926;
		int widthX = 18;
		int startZ = -953;
		int endZ = -1385;
		int widthZ = 24;
		for (int x = startX; x <= endX; x += widthX) {
		for (int z = startZ; z >= endZ; z -= widthZ) {
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
		int startX = 602;
		int endX = 926;
		int widthX = 18;
		int startZ = -953;
		int endZ = -1385;
		int widthZ = 24;
		for (int x = startX; x <= endX; x += widthX) {
		for (int z = startZ; z >= endZ; z -= widthZ) {
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
			int startX = 960;
			int endX = 1436;
			int widthX = 34;
			int startZ = -920;
			int endZ = -1361;
			int widthZ = 44;
			for (int x = startX; x <= endX; x += widthX) {
			for (int z = startZ; z >= endZ; z -= widthZ) {
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
			int startX = 960;
			int endX = 1436;
			int widthX = 34;
			int startZ = -920;
			int endZ = -1361;
			int widthZ = 44;
			for (int x = startX; x <= endX; x += widthX) {
			for (int z = startZ; z >= endZ; z -= widthZ) {
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
		
		public static void loadMAP1Signs() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=601; x<=1567; x+=14 )
			// for( int x=16; x<=1286; x+=14 )
			int startX = 601;
			int endX = 1567;
			int widthX = 14;
			int startZ = -408;
			int endZ = -852;
			int widthZ = 37;
			for (int x = startX; x <= endX; x += widthX) {

				// for( int z=-408; z>=-852; z-=37 )
				// for( int z=-18; z>=-462; z-=37 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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

							if (!NavyCraft.playerMAP1Signs.containsKey(playerName)) {
								NavyCraft.playerMAP1Signs.put(playerName, new ArrayList<Sign>());
								NavyCraft.playerMAP1Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							} else {
								NavyCraft.playerMAP1Signs.get(playerName).add(selectSign);
							NavyCraft.playerSignIndex.put(selectSign, idNum);
							}

						}
					}
				}
			}
		}

		public static Block findMAP1Open() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=601; x<=1567; x+=14 )
			// for( int x=16; x<=1286; x+=14 )
			int startX = 601;
			int endX = 1567;
			int widthX = 14;
			int startZ = -408;
			int endZ = -852;
			int widthZ = 37;
			for (int x = startX; x <= endX; x += widthX) {

				// for( int z=-408; z>=-852; z-=37 )
				// for( int z=-18; z>=-462; z-=37 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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

		public static void loadMAP2Signs() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=601; x<=1421; x+=10 )
			// for( int x=16; x<=1296; x+=10 )
			int startX = 601;
			int endX = 1421;
			int widthX = 10;
			int startZ = -356;
			int endZ = -148;
			int widthZ = 52;
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

							if (!NavyCraft.playerMAP2Signs.containsKey(playerName)) {
								NavyCraft.playerMAP2Signs.put(playerName, new ArrayList<Sign>());
								NavyCraft.playerMAP2Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							} else {
								NavyCraft.playerMAP2Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							}

						}
					}
				}
			}
		}

		public static Block findMAP2Open() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=601; x<=1421; x+=10 )
			// for( int x=16; x<=1296; x+=10 )
			int startX = 601;
			int endX = 1421;
			int widthX = 10;
			int startZ = -356;
			int endZ = -148;
			int widthZ = 52;
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

		public static void loadMAP3Signs() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=549; x>=21; x-=12 )
			// for( int x=-35; x>=-1091; x-=12 )
			int startX = 549;
			int endX = 21;
			int widthX = 12;
			int startZ = -329;
			int endZ = -92;
			int widthZ = 79;
			for (int x = startX; x >= endX; x -= widthX) {

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

							if (!NavyCraft.playerMAP3Signs.containsKey(playerName)) {
								NavyCraft.playerMAP3Signs.put(playerName, new ArrayList<Sign>());
								NavyCraft.playerMAP3Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							} else {
								NavyCraft.playerMAP3Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							}

						}
					}
				}
			}
		}

		public static Block findMAP3Open() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=549; x>=21; x-=12 )
			// for( int x=-35; x>=-1091; x-=12 )
			int startX = 549;
			int endX = 21;
			int widthX = 12;
			int startZ = -329;
			int endZ = -92;
			int widthZ = 79;
			for (int x = startX; x >= endX; x -= widthX) {
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

		public static void loadMAP4Signs() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=543; x>=21; x-=18 )
			// for( int x=-41; x>=-1085; x-=18 )
			int startX = 543;
			int endX = 21;
			int widthX = 18;
			int startZ = -408;
			int endZ = -600;
			int widthZ = 64;
			for (int x = startX; x >= endX; x -= widthX) {

				// for( int z=-408; z>=-600; z-=64 )
				// for( int z=-18; z>=-210; z-=64 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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

							if (!NavyCraft.playerMAP4Signs.containsKey(playerName)) {
								NavyCraft.playerMAP4Signs.put(playerName, new ArrayList<Sign>());
								NavyCraft.playerMAP4Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							} else {
								NavyCraft.playerMAP4Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							}

						}
					}
				}
			}
		}

		public static Block findMAP4Open() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=543; x>=21; x-=18 )
			// for( int x=-41; x>=-1085; x-=18 )
			int startX = 543;
			int endX = 21;
			int widthX = 18;
			int startZ = -408;
			int endZ = -600;
			int widthZ = 64;
			for (int x = startX; x >= endX; x -= widthX) {

				// for( int z=-408; z>=-600; z-=64 )
				// for( int z=-18; z>=-210; z-=64 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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

		public static void loadMAP5Signs() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=1404; x>=656; x-=22 )
			// for( int x=1270; x>=16; x-=22 )
			int startX = 656;
			int endX = 1426;
			int widthX = 18;
			int startZ = 142;
			int endZ = 37;
			int widthZ = 105;
			for (int x = startX; x <= endX; x += widthX) {

				// for( int z=37; z<=142; z+=105 )
				// for( int z=349; z<=454; z+=105 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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

							if (!NavyCraft.playerMAP5Signs.containsKey(playerName)) {
								NavyCraft.playerMAP5Signs.put(playerName, new ArrayList<Sign>());
								NavyCraft.playerMAP5Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							} else {
								NavyCraft.playerMAP5Signs.get(playerName).add(selectSign);
								NavyCraft.playerSignIndex.put(selectSign, idNum);
							}

						}
					}
				}
			}
		}

		public static Block findMAP5Open() {
			World syworld = plugin.getServer().getWorld("shipyard");
			// for( int x=1404; x>=656; x-=22 )
			// for( int x=1270; x>=16; x-=22 )
			int startX = 656;
			int endX = 1426;
			int widthX = 18;
			int startZ = 142;
			int endZ = 37;
			int widthZ = 105;
			for (int x = startX; x <= endX; x += widthX) {
				// for( int z=37; z<=142; z+=105 )
				// for( int z=349; z<=454; z+=105 )
				for (int z = startZ; z >= endZ; z -= widthZ) {
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
		
	public static void loadShipyard() {
		for (String s : NavyCraft.playerSHIP1Signs.keySet()) {
			NavyCraft.playerSHIP1Signs.get(s).clear();
		}
		NavyCraft.playerSHIP1Signs.clear();
		for (String s : NavyCraft.playerSHIP2Signs.keySet()) {
			NavyCraft.playerSHIP2Signs.get(s).clear();
		}
		NavyCraft.playerSHIP2Signs.clear();
		for (String s : NavyCraft.playerSHIP3Signs.keySet()) {
			NavyCraft.playerSHIP3Signs.get(s).clear();
		}
		NavyCraft.playerSHIP3Signs.clear();
		for (String s : NavyCraft.playerSHIP4Signs.keySet()) {
			NavyCraft.playerSHIP4Signs.get(s).clear();
		}
		NavyCraft.playerSHIP4Signs.clear();
		for (String s : NavyCraft.playerSHIP5Signs.keySet()) {
			NavyCraft.playerSHIP5Signs.get(s).clear();
		}
		NavyCraft.playerSHIP5Signs.clear();
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
		for (String s : NavyCraft.playerTANK2Signs.keySet()) {
			NavyCraft.playerTANK2Signs.get(s).clear();
		}
		NavyCraft.playerTANK2Signs.clear();
		for (String s : NavyCraft.playerSHIP1Signs.keySet()) {
			NavyCraft.playerSHIP1Signs.get(s).clear();
		}
		NavyCraft.playerMAP1Signs.clear();
		for (String s : NavyCraft.playerMAP2Signs.keySet()) {
			NavyCraft.playerMAP2Signs.get(s).clear();
		}
		NavyCraft.playerMAP2Signs.clear();
		for (String s : NavyCraft.playerMAP3Signs.keySet()) {
			NavyCraft.playerMAP3Signs.get(s).clear();
		}
		NavyCraft.playerMAP3Signs.clear();
		for (String s : NavyCraft.playerMAP4Signs.keySet()) {
			NavyCraft.playerMAP4Signs.get(s).clear();
		}
		NavyCraft.playerMAP4Signs.clear();
		for (String s : NavyCraft.playerMAP5Signs.keySet()) {
			NavyCraft.playerMAP5Signs.get(s).clear();
		}
		NavyCraft.playerMAP5Signs.clear();
		loadSHIP1Signs();
		loadSHIP2Signs();
		loadSHIP3Signs();
		loadSHIP4Signs();
		loadSHIP5Signs();
		loadHANGAR1Signs();
		loadHANGAR2Signs();
		loadTANK1Signs();
		loadTANK2Signs();
		loadMAP1Signs();
		loadMAP2Signs();
		loadMAP3Signs();
		loadMAP4Signs();
		loadMAP5Signs();
	}
	
	public static void loadRewards(String player) {
		NavyCraft.playerSHIP1Rewards.clear();
		NavyCraft.playerSHIP2Rewards.clear();
		NavyCraft.playerSHIP3Rewards.clear();
		NavyCraft.playerSHIP4Rewards.clear();
		NavyCraft.playerSHIP5Rewards.clear();
		NavyCraft.playerHANGAR1Rewards.clear();
		NavyCraft.playerHANGAR2Rewards.clear();
		NavyCraft.playerTANK1Rewards.clear();
		NavyCraft.playerTANK2Rewards.clear();
		NavyCraft.playerMAP1Rewards.clear();
		NavyCraft.playerMAP2Rewards.clear();
		NavyCraft.playerMAP3Rewards.clear();
		NavyCraft.playerMAP4Rewards.clear();
		NavyCraft.playerMAP5Rewards.clear();

		String worldName = "";
		if(plugin.getConfig().getString("EnabledWorlds") != "null") {
			String[] worlds = NavyCraft.instance.getConfig().getString("EnabledWorlds").split(",");
			worldName = worlds[0];
		}else{
			worldName = plugin.getServer().getPlayer(player).getWorld().getName();
		}
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if( pex==null )
			return;
		
		for(String s:PermissionsEx.getUser(player).getPermissions(worldName)) {
			if( s.contains("navycraft") ) {
				if( s.contains("ship1") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSHIP1Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s + " " + s.split(".").length);
					}
				}else if( s.contains("ship2") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSHIP2Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship3") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSHIP3Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship4") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSHIP4Rewards.put(player, num);
					} catch (Exception ex) {
						System.out.println("Invalid perm-" + s);
					}
				}else if( s.contains("ship5") )
				{
					String[] split = s.split("\\.");
					try {
						int num = Integer.parseInt(split[2]);
						NavyCraft.playerSHIP5Rewards.put(player, num);
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
				} else if( s.contains("map1") )
					{
						String[] split = s.split("\\.");
						try {
							int num = Integer.parseInt(split[2]);
							NavyCraft.playerMAP1Rewards.put(player, num);
						} catch (Exception ex) {
							System.out.println("Invalid perm-" + s + " " + s.split(".").length);
						}
					}else if( s.contains("map2") )
					{
						String[] split = s.split("\\.");
						try {
							int num = Integer.parseInt(split[2]);
							NavyCraft.playerMAP2Rewards.put(player, num);
						} catch (Exception ex) {
							System.out.println("Invalid perm-" + s);
						}
					}else if( s.contains("map3") )
					{
						String[] split = s.split("\\.");
						try {
							int num = Integer.parseInt(split[2]);
							NavyCraft.playerMAP3Rewards.put(player, num);
						} catch (Exception ex) {
							System.out.println("Invalid perm-" + s);
						}
					}else if( s.contains("map4") )
					{
						String[] split = s.split("\\.");
						try {
							int num = Integer.parseInt(split[2]);
							NavyCraft.playerMAP4Rewards.put(player, num);
						} catch (Exception ex) {
							System.out.println("Invalid perm-" + s);
						}
					}else if( s.contains("map5") )
					{
						String[] split = s.split("\\.");
						try {
							int num = Integer.parseInt(split[2]);
							NavyCraft.playerMAP5Rewards.put(player, num);
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
		if (NavyCraft.playerSHIP1Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSHIP1Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSHIP2Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSHIP2Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSHIP3Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSHIP3Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSHIP4Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSHIP4Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerSHIP5Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerSHIP5Signs.get(player)) {
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
		if ((foundSign == null) && NavyCraft.playerTANK2Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerTANK2Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerMAP1Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerMAP1Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerMAP2Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerMAP2Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerMAP3Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerMAP3Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerMAP4Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerMAP4Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		if ((foundSign == null) && NavyCraft.playerMAP5Signs.containsKey(player)) {
			for (Sign s : NavyCraft.playerMAP5Signs.get(player)) {
				if (id == NavyCraft.playerSignIndex.get(s)) {
					foundSign = s;
				}
			}
		}
		return foundSign;
	}

	public static int maxId(Player player) {
		int foundHighest = -1;
		if (NavyCraft.playerSHIP1Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSHIP1Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSHIP2Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSHIP2Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSHIP3Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSHIP3Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSHIP4Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSHIP4Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerSHIP5Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerSHIP5Signs.get(player.getName())) {
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
		if (NavyCraft.playerTANK2Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerTANK2Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerMAP1Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerMAP1Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerMAP2Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerMAP2Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerMAP3Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerMAP3Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerMAP4Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerMAP4Signs.get(player.getName())) {
				if (foundHighest < NavyCraft.playerSignIndex.get(s)) {
					foundHighest = NavyCraft.playerSignIndex.get(s);
				}
			}
		}
		if (NavyCraft.playerMAP5Signs.containsKey(player.getName())) {
			for (Sign s : NavyCraft.playerMAP5Signs.get(player.getName())) {
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
	public static void showRank(Player player, String p) {
		int exp = 0;
		int exp1 = 0;
		String worldName = null;
		
		NavyCraft.loadExperience();
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		
		int rankExp=0;
		for(String s:PermissionsEx.getUser(p).getPermissions(worldName)) {
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
		
		List<String> groupNames = PermissionsEx.getUser(p).getParentIdentifiers("navycraft");
		for( String s : groupNames ) {
			if( PermissionsEx.getPermissionManager().getGroup(s).getRankLadder().equalsIgnoreCase("navycraft") ) {
				if (NavyCraft.playerExp.containsKey(p)) {
					exp = NavyCraft.playerExp.get(p);
				}
				player.sendMessage(ChatColor.GRAY + p + "'s rank is " + ChatColor.WHITE + s.toUpperCase()
						+ ChatColor.GRAY + " and has " + ChatColor.WHITE + exp + "/" + rankExp
						+ ChatColor.GRAY + " rank points.");
				return;
	   } else { 
		   exp1 = NavyCraft.playerExp.get(p);
			String[] groupName = PermissionsEx.getUser(p).getGroupsNames();
			for( String g : groupName ) {
			player.sendMessage(ChatColor.GRAY + p + "'s rank is " + ChatColor.WHITE + g.toUpperCase()
			+ ChatColor.GRAY + " and has " + ChatColor.WHITE + exp1
			+ ChatColor.GRAY + " rank points.");
	return;
	       }
		}
	}
}
	public static void getRank(Player player) {
		int exp = 0;
		int exp1 = 0;
		String worldName = player.getWorld().getName();
		
		NavyCraft.loadExperience();
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		
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
							checkRankWorld(player, exp, player.getWorld());
				}
			   } else { 
					if (NavyCraft.playerExp.containsKey(player.getName())) {
					exp1 = NavyCraft.playerExp.get(player.getName());
					}
					String[] groupName = PermissionsEx.getUser(player).getGroupsNames();
					for( String g : groupName ) {
					player.sendMessage(ChatColor.GRAY + "Your rank is " + ChatColor.WHITE + g.toUpperCase()
					+ ChatColor.GRAY + " and you have " + ChatColor.WHITE + exp1
					+ ChatColor.GRAY + " rank points.");
				return;
			}
	   }
	}
}
	
	public static void rewardExpPlayer(int newExp, Player player) {
		 if (NavyCraft.playerExp.containsKey(player.getName())) {
			newExp = NavyCraft.playerExp.get(player.getName()) + newExp;
			NavyCraft.playerExp.put(player.getName(), newExp);
		} else {
			NavyCraft.playerExp.put(player.getName(), newExp);
		}
		
		player.sendMessage(ChatColor.GRAY + "You now have " + ChatColor.WHITE + newExp + ChatColor.GRAY + " rank points.");
			
		NavyCraft_BlockListener.checkRankWorld(player, newExp, player.getWorld());
		NavyCraft.saveExperience();	
		if (NavyCraft.battleMode > 0) {
		if (NavyCraft.battleType == 1) {		
            if (NavyCraft.redPlayers.contains(player.getName())) {		
            NavyCraft.redPoints += newExp;		
            } else {		
            NavyCraft.bluePoints += newExp;		
            }
		}
	}
		
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
				checkRankWorld(p, playerNewExp, craft.world);
			}
		NavyCraft.saveExperience();
		if (NavyCraft.battleMode > 0) {
		if (NavyCraft.battleType == 1) {		
            if (NavyCraft.redPlayers.contains(p.getName())) {		
            NavyCraft.redPoints += newExp;		
            } else {		
            NavyCraft.bluePoints += newExp;		
            }
		}
	}
}
	}
	
	public static void setExpPlayer(int newExp, String p) {
		NavyCraft.playerExp.put(p, newExp);
		NavyCraft.saveExperience();
	}
	
	public static void removeExpPlayer(int newExp, String p) {
		if (NavyCraft.playerExp.containsKey(p)) {
			newExp = NavyCraft.playerExp.get(p) - newExp;
			NavyCraft.playerExp.put(p, newExp);
		} else {
			NavyCraft.playerExp.put(p, newExp);
		}
		NavyCraft.saveExperience();
	}
	
	public static void addExpPlayer(int newExp, String p) {
		if (NavyCraft.playerExp.containsKey(p)) {
			newExp = NavyCraft.playerExp.get(p) + newExp;
			NavyCraft.playerExp.put(p, newExp);
		} else {
			NavyCraft.playerExp.put(p, newExp);
		}
		NavyCraft.saveExperience();
	}
	public static void checkRankWorld(Player playerIn, int newExp, World world) {
		String worldName = world.getName();
		
		pex = (PermissionsEx)plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if( pex==null )
			return;
		
		for(String s:PermissionsEx.getUser(playerIn).getPermissions(worldName)) {
			if( s.contains("navycraft") ) {
				if( s.contains("exp") ) {
					String[] split = s.split("\\.");
					try {
						int rankExp = Integer.parseInt(split[2]);
						if( newExp >= rankExp ) {
							PermissionsEx.getUser(playerIn).promote(null, "navycraft");
							
							String rankName = "";
							List<String> groupNames = PermissionsEx.getUser(playerIn).getParentIdentifiers("navycraft");
							for( String group : groupNames ) {
								if( PermissionsEx.getPermissionManager().getGroup(group).getRankLadder().equalsIgnoreCase("navycraft") ) {
									rankName = group;
									break;
								}
							}
							plugin.getServer().broadcastMessage(ChatColor.GREEN + playerIn.getName() + " has been promoted to the rank of " + ChatColor.YELLOW + rankName.toUpperCase() + ChatColor.GREEN + "!");
						}
						
							
					} catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("Invalid perm-" + s);
					}
				}
			}
		}
	}
}
