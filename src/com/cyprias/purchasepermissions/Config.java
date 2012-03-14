package com.cyprias.purchasepermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

	public static String locale;
	public static boolean autoLoadDefaultLocales;

	private static Configuration config;
	static Logger log = Logger.getLogger("Minecraft");
	private static List<String> list;

	public static String notifyPurchase;

	public boolean useBuyPermission;

	
	//File configFile = null;
	public Config(PurchasePermissions plugin) {
		this.plugin = plugin;

		//configFile = new File(plugin.getDataFolder(), "config.yml");
		//if (!configFile.exists()) {
			//log.info("CONFIG DOESN'T EXIST!");
		//}
		
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

		useBuyPermission = config.getBoolean("useBuyPermission");

		locale = config.getString("locale");
		autoLoadDefaultLocales = config.getBoolean("autoLoadDefaultLocales");

		loadPermissionsFile();
		
	}

	public void reloadOurConfig(){
		plugin.reloadConfig();
		config = plugin.getConfig().getRoot();
	}
	
	private String L(String key) {
		return plugin.L(key);
	}

	public String F(String key, Object... args) {
		return plugin.F(key, args);
	}

	public boolean permissionExists(String pName) {
		return (permissions.getConfigurationSection("permissions." + pName) != null);
	}

	public boolean createPermission(Player sender, String permissionName) {
		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + F("stPermissionAlreadyExists", permissionName));
			return false;
		}
		permissions.getConfigurationSection("permissions").createSection(permissionName);
		
		plugin.saveConfig();
		return true;
	}

	public boolean modifyPermissionSetting(Player sender, String oName, String oSetting, String oValue) {
		if (permissions.getConfigurationSection("permissions." + oName) == null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + F("stPermNoExist", oName));
			return false;
		}

		ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(oName.toLowerCase());

		groupSection.set(oSetting.toLowerCase(), oValue);

		plugin.saveConfig();
		return true;
	}

	public boolean removePermission(Player sender, String permName) {
		if (permissions.getConfigurationSection("permissions." + permName) == null) {
			sender.sendMessage(PurchasePermissions.chatPrefix + F("stPermNoExist", permName));
			return false;
		}

		permissions.getConfigurationSection("permissions").set(permName, null);
		plugin.saveConfig();

		return true;
	}

	public Set<String> getPermissions() {
		if (permissions.isSet("permissions"))
			return permissions.getConfigurationSection("permissions").getKeys(false);
		
		return null;
	}

	public static class permissionInfo {
		String name;
		List<String> node;
		List<String> world;
		String command;
		int price;
		int duration;
		int uses;
		boolean requirebuypermission;
	}


	public permissionInfo isValidCommand(String message) throws Exception {
		Set<String> permissions = getPermissions();

		// Config.permissionInfo info;
		// boolean found=false;
		for (Object o : permissions) {
			String command = plugin.config.getPermisionCommand(o.toString());

			if (command != null && message.toLowerCase().contains(command.toLowerCase())) {
				return getPermissionInfo(o.toString());
				// return o.toString();
			}

			// for (String permissionName : info.node) {
			// log.info(info + permissionName);
			//
			//
			//
			// }

		}

		return null;
	}

	public boolean canUsePermissionInWorld(Player player, String permissionName) {
		String aWorld = player.getLocation().getWorld().getName().toString();

		// log.info(plugin.chatPrefix + "canUsePermissionInWorld: 1 " +
		// permissionName + " " + aWorld);
		List<String> worlds = getPermissionWorlds(permissionName);
		if (worlds == null) {
			return true;
		}
		for (String wName : worlds) {
			if (aWorld.equalsIgnoreCase(wName)) {
				return true;
			}
		}

		return false;
	}

	public List<String> getPermissionWorlds(String permissionName) {
		List<String> worlds = null;

		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			if (groupSection.isSet("world"))
				/**/
				if (groupSection.isList("world")) {
					worlds = groupSection.getStringList("world");
				} else if (groupSection.isString("world")) {

					worlds = new ArrayList();

					String sWorld = groupSection.getString("world");

					if (sWorld.contains(",")) {
						String[] temp = sWorld.split(",");
						for (int i = 0; i < temp.length; i++)
							worlds.add(temp[i].trim());
					} else
						worlds.add(sWorld);

				}

		}
		return worlds;
	}

	
	
	public permissionInfo getPermissionInfo(String permissionName) throws Exception {

		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(permissionName);

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

			if (groupSection.isSet("node"))
				/**/
				if (groupSection.isList("node")) {
					myReturner.node = groupSection.getStringList("node");
				} else if (groupSection.isString("node")) {

					myReturner.node = new ArrayList();

					String sNode = groupSection.getString("node");

					if (sNode.contains(",")) {
						String[] temp = sNode.split(",");
						for (int i = 0; i < temp.length; i++)
							myReturner.node.add(temp[i].trim());
					} else
						myReturner.node.add(sNode);

				}

			if (groupSection.isSet("world"))
				/**/
				if (groupSection.isList("world")) {
					myReturner.world = groupSection.getStringList("world");
				} else if (groupSection.isString("world")) {

					myReturner.world = new ArrayList();

					String sWorld = groupSection.getString("world");

					if (sWorld.contains(",")) {
						String[] temp = sWorld.split(",");
						for (int i = 0; i < temp.length; i++)
							myReturner.world.add(temp[i].trim());
					} else
						myReturner.world.add(sWorld);

				}

			myReturner.requirebuypermission = false;
			if (groupSection.isSet("requirebuypermission"))
				myReturner.requirebuypermission = Boolean.valueOf(groupSection.get("requirebuypermission").toString());
			
			
			
			
			
			return myReturner;

		}

		/*
		 * if (permissions.getConfigurationSection("permissions." + permissionName)
		 * != null) { permissionInfo myReturner = new permissionInfo(); try {
		 * Map<String, Object> permissions2 =
		 * permissions.getConfigurationSection("permissions." +
		 * permissionName).getValues(false); myReturner.name = (String)
		 * permissionName;
		 * 
		 * myReturner.node = (List<String[]>) permissions2.get("node");
		 * 
		 * myReturner.command = (String) permissions2.get("command");
		 * myReturner.price = (Integer) permissions2.get("price");
		 * myReturner.duration = (Integer) permissions2.get("duration");
		 * myReturner.uses = (Integer) permissions2.get("uses");
		 * 
		 * return myReturner; } catch (Exception e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } }
		 */

		return null;

	}

	public boolean isValidSetting(String oName) {
		// log.info("isValidSetting oName: " + oName);

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
		else if (oName.equalsIgnoreCase("world"))
			return true;
		else if (oName.equalsIgnoreCase("requirebuypermission"))
			return true;
		
		return false;
	}

	public List getPermissionNode(String permissionName) {

		List node = null;

		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(permissionName);

			if (groupSection.isList("node")) {
				node = groupSection.getStringList("node");

			} else if (groupSection.isString("node")) {
				// log.info("String! " + groupSection.getString("node"));
				node = new ArrayList();
				node.add(groupSection.getString("node"));
			}

		}

		return node;
	}

	public String getPermisionCommand(String permissionName) {

		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(permissionName);

			return groupSection.getString("command");

		}

		return null;
	}

	public String getPermisionPayTo(String permissionName) {
		
		if (permissions.getConfigurationSection("permissions." + permissionName) != null) {
			permissionInfo myReturner = new permissionInfo();
			ConfigurationSection groupSection = permissions.getConfigurationSection("permissions").getConfigurationSection(permissionName);
			return groupSection.getString("payto");

		}

		return null;
	}

	
	File permissionsFile = null;
	FileConfiguration permissions = new YamlConfiguration();
	public void loadPermissionsFile() {
		if (permissionsFile == null) {
			reloadPermissionsFile();
		}

		
		
		
	}
	
	private void reloadPermissionsFile() {
		try {
			permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");

			// Check if file exists in plugin dir, or create it.
			if (!permissionsFile.exists()) {
				permissionsFile.getParentFile().mkdirs();
				copy(plugin.getResource("permissions.yml"), permissionsFile);

			}

			permissions.load(permissionsFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
