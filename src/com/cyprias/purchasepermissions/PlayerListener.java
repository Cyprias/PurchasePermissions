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
import org.bukkit.event.player.PlayerTeleportEvent;
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
		plugin.unregisterPlayer(event.getPlayer());

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled())
			return;
		plugin.unregisterPlayer(event.getPlayer());

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLogin(PlayerJoinEvent event) {
		plugin.registerPlayer(event.getPlayer());
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
		commandInfo newCmd = new commandInfo();

		newCmd.player = event.getPlayer();
		newCmd.message = event.getMessage().toString();

		usedCommands.add(newCmd);

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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPermission(String pName, String permissionName) {
		//if (PurchasePermissions.permissions.containsKey(pName)) {
			List<String> nodes = plugin.config.getPermissionNode(permissionName);
			

			if (nodes != null) {

				
				PermissionAttachment attachment = PurchasePermissions.permissions.get(pName);
	
				// log.info("Player addPermission 6");
				if (attachment != null){
					for (String nodeName : nodes) {
						plugin.info(" Adding " + nodeName + " to " + pName + ".");
						attachment.setPermission(nodeName, true);
					}
				}
				
			}
		//}
	}

	public void removePermission(String pName, String permissionName) {
		//if (PurchasePermissions.permissions.containsKey(pName)) {
			List<String> nodes = plugin.config.getPermissionNode(permissionName);
			// log.info("Player addPermission 3");
			if (nodes != null) {

				
			//	plugin.info(" Removing " + nodes + " to " + pName + ".");
				PermissionAttachment attachment = PurchasePermissions.permissions.get(pName);
				// log.info("Player addPermission 6");
	
				if (attachment != null){
					for (String nodeName : nodes) {
						attachment.unsetPermission(nodeName);
					}
				}
			}
		//}
	}
	
}
