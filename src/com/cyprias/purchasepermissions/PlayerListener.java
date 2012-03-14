package com.cyprias.purchasepermissions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import com.cyprias.purchasepermissions.Config.permissionInfo;
import com.cyprias.purchasepermissions.PlayerListener.commandInfo;

class PlayerListener implements Listener {
	private PurchasePermissions plugin;
	static Logger log = Logger.getLogger("Minecraft");

	public PlayerListener(PurchasePermissions plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		// log.info("Player " + event.getPlayer().getName() +
		// " quit, unregistering...");
		plugin.unregisterPlayer(event.getPlayer());

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled())
			return;
		// log.info("Player " + event.getPlayer().getName() +
		// " was kicked, unregistering...");
		plugin.unregisterPlayer(event.getPlayer());

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLogin(PlayerJoinEvent event) {
		// log.info("Player " + event.getPlayer().getName() +
		// " joined, registering...");
		plugin.registerPlayer(event.getPlayer());
		// pb.loadPlayerPermissions(event.getPlayer().getName());
		// log.info("Player " + event.getPlayer().getName() +
		// " joined, registering... 2");

		// addPermission(event.getPlayer().getName(), "time");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		plugin.resetPlayerPermissions(event.getPlayer());
	}
	
	
	public List<commandInfo> usedCommands = new ArrayList<commandInfo>();

	static class commandInfo {
		Player player;
		String message;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		// log.info("onPlayerCommand " + event.getPlayer().getName() + " - " +
		// event.getMessage().toString()) ;
		// log.info("getName " + event.getPlayer().getName());
		// log.info("getMessage " + event.getMessage());
		// log.info("toString " + event.getMessage().toString());

		commandInfo newCmd = new commandInfo();

		newCmd.player = event.getPlayer();
		newCmd.message = event.getMessage().toString();

		// log.info("size 1 " + usedCommands.size());

		
		
		usedCommands.add(newCmd);
		// log.info("size 2 " + usedCommands.size());

		try {

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				public void run() {

					for (int i = usedCommands.size() - 1; i >= 0; i--) {
						// plugin.getServer().broadcastMessage(i + ": " +
						// usedCommands.get(i).playerName + " said " +
						// usedCommands.get(i).message);

						try {
							plugin.database.commandUsed(usedCommands.get(i).player, usedCommands.get(i).message);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						usedCommands.remove(i);
					}

				}
			}, 0L);

			// database.commandUsed(event.getPlayer().getName(),
			// event.getMessage().toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPermission(String pName, String permissionName) {
		//if (PurchasePermissions.permissions.containsKey(pName)) {
			List<String> nodes = plugin.config.getPermissionNode(permissionName);
			
			
			// log.info("Player addPermission 3");
			if (nodes != null) {
				// log.info("Player addPermission 4");
	
				// Player player = plugin.getServer().getPlayer(pName);
				// log.info("Player addPermission 5");
				// if (player != null){
				log.info(PurchasePermissions.chatPrefix + " Adding " + nodes + " to " + pName + ".");
				PermissionAttachment attachment = PurchasePermissions.permissions.get(pName);
	
				// log.info("Player addPermission 6");
	
				for (String nodeName : nodes) {
					attachment.setPermission(nodeName, true);
				}
	
				// log.info("Player addPermission 7");
				// }
			}
		//}
	}

	public void removePermission(String pName, String permissionName) {
		//if (PurchasePermissions.permissions.containsKey(pName)) {
			List<String> nodes = plugin.config.getPermissionNode(permissionName);
			// log.info("Player addPermission 3");
			if (nodes != null) {

				
				log.info(PurchasePermissions.chatPrefix + " Removing " + nodes + " to " + pName + ".");
				PermissionAttachment attachment = PurchasePermissions.permissions.get(pName);
				// log.info("Player addPermission 6");
	
				for (String nodeName : nodes) {
					attachment.unsetPermission(nodeName);
				}

			}
		//}
	}
	
}
