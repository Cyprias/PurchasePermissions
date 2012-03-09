package com.cyprias.purchasepermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.cyprias.purchasepermissions.PurchasePermissions;

public class Config {

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

	public Set<String> getPermissions() {
		Set<String> permissions = config.getConfigurationSection("permissions").getKeys(false);
		return permissions;
	}

	static class permissionInfo {
		String name;
		List<String> node;
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


	public static permissionInfo getPermissionInfo(String permissionName) throws Exception {

		if (config.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = config.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			
			myReturner.name = (String) permissionName;
			myReturner.command = (String) groupSection.get("command");
			myReturner.price = (Integer) groupSection.get("price");
			myReturner.duration = (Integer) groupSection.get("duration");
			myReturner.uses = (Integer) groupSection.get("uses");
			
			
			
			if (groupSection.isList("node")) {
				myReturner.node = groupSection.getStringList("node");
			
			}else if (groupSection.isString("node")) {
				//log.info("String! " + groupSection.getString("node"));
				myReturner.node = new ArrayList();
				myReturner.node.add(groupSection.getString("node"));
				
			//	myReturner.node.add(e)
				
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
	
	public static String getPermisionCommand (String permissionName){
		
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
