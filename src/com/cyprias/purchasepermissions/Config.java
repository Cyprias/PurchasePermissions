package com.cyprias.purchasepermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cyprias.purchasepermissions.PurchasePermissions;

public class Config extends JavaPlugin {
	private PurchasePermissions plugin;


	
	private static final boolean String = false;
	// public static String adminGroupPermission;
	// public static String leadershipGroupPermission;
	public static String DbUrl;
	public static String DbUser;
	public static String DbPassword;
	public static String DbDatabase;
	public static String DbTable;

	private static Configuration config;
	static Logger log = Logger.getLogger("Minecraft");
	private static List<String> list;

	public static String notifyPurchase;
	
	public Config(PurchasePermissions plugin) {
		this.plugin = plugin;
		
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		// config.set("version", plugin.version);
		plugin.saveConfig();

		DbUser = config.getString("mysql.username");
		DbPassword = config.getString("mysql.password");
		DbUrl = "jdbc:mysql://" + config.getString("mysql.hostname") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database");
		DbDatabase = config.getString("mysql.database");
		DbTable = config.getString("mysql.table");
		
		notifyPurchase = config.getString("notifyPurchase");
		
		/*
		 * Set<String> permissions =
		 * config.getConfigurationSection("permissions").getKeys(false);
		 * 
		 * for (Object o : permissions) { String e = o.toString(); log.info(e);
		 * }
		 * 
		 * Map<String, Object> permissions2 =
		 * config.getConfigurationSection("permissions.time").getValues(false);
		 * 
		 * 
		 * 
		 * 
		 * log.info("node: " + permissions2.get("node")); for (Map.Entry<String,
		 * Object> entry : permissions2.entrySet()) { log.info(entry.getKey() +
		 * ": " + entry.getValue().toString());
		 * 
		 * }
		 */

	}

	public boolean permissionExists(String pName){
		return (config.getConfigurationSection("permissions." + pName) != null);
	}
	
	public boolean createPermission(Player sender, String permissionName){
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + "That permission name is already used.");
			return false;
		}
		config.getConfigurationSection("permissions").createSection(permissionName);
		plugin.saveConfig();
		return true;
	}
	

	
	public boolean modifyPermissionSetting(Player sender, String oName, String oSetting, String oValue){
		if (config.getConfigurationSection("permissions." + oName) == null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + "That permission does not exist.");
			return false;
		}

		ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(oName.toLowerCase());
		
		groupSection.set(oSetting.toLowerCase(), oValue);

		plugin.saveConfig();
		return true;
	}
	
	public boolean removePermission(Player sender, String permName){
		if (config.getConfigurationSection("permissions." + permName) == null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + "That permission does not exist.");
			return false;
		}
		
		config.getConfigurationSection("permissions").set(permName, null);
		plugin.saveConfig();
		
		return true;
	}
	
	public Set<String> getPermissions() {
		return config.getConfigurationSection("permissions").getKeys(false);
	}

	public static class permissionInfo {
		String name;
		List<String> node;
		List<String> world;
		String command;
		int price;
		int duration;
		int uses;
	}

	public static void testList(){
		
		permissionInfo myReturner = new permissionInfo();

		//log.info("testList 1");
		
		Set<String> permissions = config.getConfigurationSection("permissions").getKeys(false);
		//log.info("testList 2");
		for (String permissionName : permissions) {
			//log.info("testList 3");
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			//log.info("testList 4");
			
			
			log.info(permissionName + ". isList: " + groupSection.isList("node") );
			log.info(permissionName + ". isString: " + groupSection.isString("node") );
			
			/*
			List<String> nodes = groupSection.getStringList("node");
			//log.info("testList 5");
			for (String nodeName : nodes) {
				
				log.info(permissionName + " " + nodeName);
				
			}
			*/
		}
			
			
			
			//ConfigurationSection groupSection = config.getConfigurationSection("permissions." + permissionName).getConfigurationSection("node");
			//List<String> groupPlayers = groupSection.getList("players");
			
			//myReturner.node = (List<String[]>) permissions2.get("node");
			
	}

	public permissionInfo isValidCommand(String message) throws Exception{
		Set<String> permissions = getPermissions();
		
		Config.permissionInfo info;
		boolean found=false;
		for (Object o : permissions) {
			String command = plugin.config.getPermisionCommand(o.toString());
			
			
			if (command != null && message.toLowerCase().contains(command.toLowerCase())) {
				return getPermissionInfo(o.toString());
				//return o.toString();
			}
			
			
			//for (String permissionName : info.node) {
			//	log.info(info + permissionName);
			//	
			//	
			//	
			//}
			
		}
		
		return null;
	}
	
	public boolean canUsePermissionInWorld(Player player, String permissionName){
		String aWorld = player.getLocation().getWorld().getName().toString();
		//log.info(plugin.chatPrefix + "canUsePermissionInWorld: 1 " + permissionName + " " + aWorld);
		List<String> worlds = getPermissionWorlds(permissionName);
		if (worlds == null){
			return true;
		}
		for (String wName : worlds) {
			//log.info(node + permissionName);
			if (aWorld.equalsIgnoreCase(wName)) {
				return true;
			}
		}

		return false;
	}
	
	public List<String> getPermissionWorlds (String permissionName){
		List<String> worlds = null;
		
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			if (groupSection.isList("world")) {
				worlds = groupSection.getStringList("world");
			
			}else if (groupSection.isString("world")) {
				worlds = new ArrayList();
				worlds.add(groupSection.getString("world"));
			}
		}
		return worlds;
	}
	
	
	
	public static permissionInfo getPermissionInfo(String permissionName) throws Exception {

		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			
			myReturner.name = (String) permissionName;
			if (groupSection.isSet("command"))
				myReturner.command = (String) groupSection.get("command");

			myReturner.price = 0;
			if (groupSection.isSet("price"))
				myReturner.price = Integer.valueOf(groupSection.get("price").toString());

			myReturner.duration = 0;
			if (groupSection.isSet("duration"))
				myReturner.duration = Integer.valueOf(groupSection.get("duration").toString());

			myReturner.uses = 0;
			if (groupSection.isSet("uses"))
				myReturner.uses = Integer.valueOf(groupSection.get("uses").toString());
			
			
			if (groupSection.isList("node")) {
				myReturner.node = groupSection.getStringList("node");
			
			}else if (groupSection.isString("node")) {
				//log.info("String! " + groupSection.getString("node"));
				myReturner.node = new ArrayList();
				myReturner.node.add(groupSection.getString("node"));
				
			//	myReturner.node.add(e)
				
			}
			
			if (groupSection.isList("world")) {
				myReturner.world = groupSection.getStringList("world");
			
			}else if (groupSection.isString("world")) {
				myReturner.world = new ArrayList();
				myReturner.world.add(groupSection.getString("world"));
			}
			
			return myReturner;
			
		}
		
		/*
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			try {
				Map<String, Object> permissions2 = config.getConfigurationSection("permissions." + permissionName).getValues(false);
				myReturner.name = (String) permissionName;

				myReturner.node = (List<String[]>) permissions2.get("node");

				myReturner.command = (String) permissions2.get("command");
				myReturner.price = (Integer) permissions2.get("price");
				myReturner.duration = (Integer) permissions2.get("duration");
				myReturner.uses = (Integer) permissions2.get("uses");

				return myReturner;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 */
		
		return null;

	}

	public boolean isValidSetting(String oName){
		log.info("isValidSetting oName: " + oName);

		if (oName.equalsIgnoreCase("node"))
			return true;
		else if (oName.equalsIgnoreCase("command"))
			return true;
		else if (oName.equalsIgnoreCase("price"))
			return true;
		else if (oName.equalsIgnoreCase("uses"))
			return true;
		else if (oName.equalsIgnoreCase("duration"))
			return true;
		else if (oName.equalsIgnoreCase("payto"))
			return true;
		
		return false;
	}
	

	public static List getPermissionNode (String permissionName){
		
		List node = null;
		
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			
			if (groupSection.isList("node")) {
				node = groupSection.getStringList("node");
			
			}else if (groupSection.isString("node")) {
				//log.info("String! " + groupSection.getString("node"));
				node = new ArrayList();
				node.add(groupSection.getString("node"));
			}
			
		}

		return node;
	}
	
	public String getPermisionCommand (String permissionName){
		
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			
			return groupSection.getString("command");
			
		}
		
		return null;
	}
	
	public static String getPermisionPayTo (String permissionName){
		
		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			return groupSection.getString("payto");
			
		}
		
		return null;
	}
	
}
