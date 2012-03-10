package com.cyprias.purchasepermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

//import com.cyprias.purchasepermissions.economy;
public class PurchasePermissions extends JavaPlugin {
	public static File folder = new File("plugins/PurchasePermissions");

	public static String chatPrefix = "§f[§aPP§f] ";

	public static String perm_create = "purchasepermissions.create";
	public static String perm_modify = "purchasepermissions.modify";
	public static String perm_remove = "purchasepermissions.remove";

	private String stPermModifed = chatPrefix + "§f%s§7's §f%s §7is set to §f%s§7.";
	private String stPluginEnabled = chatPrefix + "§f%s §7v§f%s §7is enabled.";
	private String stPurchaseNotify = chatPrefix + "§f%s §7has purchased §f%s§7 for §f%s§7.";
	private String stAvailableCommands1 = chatPrefix + "§a/pp list §f- List all permissions.";
	private String stAvailableCommands2 = chatPrefix + "§a/pp info <permission> §f- Get info on a permission.";
	private String stAvailableCommands3 = chatPrefix + "§a/pp buy <permission> §7[player]§f §f- Buy a permission.";
	private String stAvailableCommands4 = chatPrefix + "§a/pp active §f- Display your active permissions.";

	private String stAvailableCommandsCreate = chatPrefix + "§a/pp create <permission> §f- Create a new permission.";
	private String stAvailableCommandsModify = chatPrefix + "§a/pp modify <permission> <setting> <value> §f- Modify a permission.";
	private String stAvailableCommandsRemove = chatPrefix + "§a/pp remove <permission> §f- remove a permission.";

	private String stAvailablePermissions = chatPrefix + " §7Available permissions";
	private String stIncludePermissionName = chatPrefix + " §7You need to include the permission's name.";
	private String stPermissionInfo = chatPrefix + "%s §7Info.";
	private String stPermissionInfoNode = "  §7node: %s";
	private String stPermissionInfoCommand = "  §7command: %s";
	private String stPermissionInfoPrice = "  §7price: $%s";
	private String stPermissionInfoDuration = "  §7duration: %s";
	private String stPermissionInfoUse = "  §7uses: %s";
	private String stPermNoExist = chatPrefix + "§7Permission '§f%s§7' does not exist, try again.";
	private String stIncludeName = chatPrefix + "§7You need to include a name.";
	private String stPermCreated = chatPrefix + "§7Permission created, use /pp modify to set it's settings.";
	private String stAvailableSettings = chatPrefix + "§7Available settings";
	private String stAvailableSettingsNode = "  §anode §7- The permission node users will receive.";
	private String stAvailableSettingsPrice = "  §aprice §7- The cost of the permision.";
	private String stAvailableSettingsDuration = "  §aduration §7- How long users should have the permission (in minutes).";
	private String stAvailableSettingsCommand = "  §acommand §7- command that's associated with the permision";
	private String stAvailableSettingsUses = "  §auses §7- How many times a user can use that command.";
	private String stAvailableSettingsPayto = "  §apayto §7- Who should recieve the funds from the permission purchase. (default noone)";
	private String stPermNoExists = chatPrefix + "§f%s §7does not exist, you need to create it first.";
	private String stIncludeSettingName = chatPrefix + "§7You need to include a setting name. (§f/pp modify§7 for a list)";
	private String stNotValidSetting = chatPrefix + "§f%s§7 is not a valid permission setting.";
	private String stIncludeSettingPerm = chatPrefix + "§fYou need to include the value for §f%s.";
	private String stIncludePermName = chatPrefix + "§fYou need to include the permission's name.";
	private String stAlreadyOwnPerm = chatPrefix + "§fYou already own that permission.";
	private String stUserBoughtPerm = chatPrefix + "%s bought %s for %s.";
	private String stPaidUser = chatPrefix + "§7Paid §f%s §7$§f%d§7.";
	private String stPurchasedPerm = chatPrefix + "§f%s §7purchased for §f%s§7.";
	private String stFailedToBuyPerm = chatPrefix + "§7failed to buy §f%s§7.";
	private String stNotEnoughFunds = chatPrefix + "§7You don't have enought funds to buy §f%s§7.";
	private String stPermRemoved = chatPrefix + "§f%s §7has been removed.";
	
	Logger log = Logger.getLogger("Minecraft");
	public String name;
	public String version;
	public Config config;

	public static Server server;

	public static Economy econ = null;

	private PlayerListener playerListener = new PlayerListener(this);

	HashMap<String, PermissionAttachment> activePermissions;
	private PurchasePermissions plugin;

	public static HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();

	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		server = getServer();
		name = this.getDescription().getName();
		version = this.getDescription().getVersion();

		// Load config and permissions
		config = new Config(this);

		// Register stuff
		getServer().getPluginManager().registerEvents(playerListener, this);

		/*
		 * if (!setupEconomy()) { log.info(String.format(
		 * "[%s] - Disabled due to no Vault dependency found!",
		 * getDescription().getName()));
		 * getServer().getPluginManager().disablePlugin(this); return; }
		 */
		try {
			database.setupMysql();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {
				// System.out.println("This message is printed by an async thread");
				try {
					database.cleanOldEntries();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 100L, 200L); // 1200

		for (Player p : getServer().getOnlinePlayers()) {
			registerPlayer(p);
		}

		log.info(String.format(stPluginEnabled, name, version));
	}

	protected void registerPlayer(Player player) {
		// log.info(chatPrefix + "Registering " + player.getName());
		if (permissions.containsKey(player.getName())) {
			log.info(chatPrefix + "Registering " + player.getName() + ": was already registered");
			unregisterPlayer(player);
		}
		PermissionAttachment attachment = player.addAttachment(this);

		permissions.put(player.getName(), attachment);

		try {
			database.retrieveActivePermissions(player.getName());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void unregisterPlayer(Player player) {
		// log.info(chatPrefix + "Unegistering " + player.getName());
		if (permissions.containsKey(player.getName())) {

			try {
				database.removeActivePermissions(player.getName());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				player.removeAttachment(permissions.get(player.getName()));
			} catch (IllegalArgumentException ex) {
				log.info("Unregistering " + player.getName() + ": player did not have attachment");
			}

			permissions.remove(player.getName());
		}
	}

	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			unregisterPlayer(p);
		}
		log.info(chatPrefix + name + " v" + version + " is disabled.");

	}

	public boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public double getBalance(String pName) {
		if (setupEconomy()) {
			return econ.getBalance(pName);
		}
		return 0;
	}

	public boolean payPlayer(String pName, double amount) {
		if (setupEconomy()) {
			// return econ.getBalance(pName);

			econ.depositPlayer(pName, amount);
			return true;
		}
		return false;
	}

	public void notifyUsersOfPurchase(Player sender, String permissionName, String target) {
		for (Player oPlayer : getServer().getOnlinePlayers()) {
			if (sender != oPlayer) {
				if (oPlayer.hasPermission(Config.notifyPurchase)) {

					oPlayer.sendMessage(String.format(stPurchaseNotify, sender.getDisplayName(), permissionName, target));
					// } else {
					// log.info(oPlayer.getName() + " does not have access to "
					// +
					// Config.notifyPurchase);

				}
			}

		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;

		String senderName = "[SERVER]";

		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getDisplayName();
		}

		final String message = getFinalArg(args, 0);

		log.info(chatPrefix + senderName + " " + cmd.getName() + ": " + message.toString());

		if (cmd.getName().equalsIgnoreCase("pp")) {
			if (args.length == 0) {
				// sender.sendMessage(chatPrefix + "Include a message.");
				// log.info(chatPrefix + "/pp list - List all permissions.");

				sender.sendMessage(stAvailableCommands1);
				sender.sendMessage(stAvailableCommands2);
				sender.sendMessage(stAvailableCommands3);
				sender.sendMessage(stAvailableCommands4);

				if (player.hasPermission(perm_create))
					player.sendMessage(stAvailableCommandsCreate);
				if (player.hasPermission(perm_modify))
					player.sendMessage(stAvailableCommandsModify);
				if (player.hasPermission(perm_remove))
					player.sendMessage(stAvailableCommandsRemove);

				return true;
			}

			if (args[0].equalsIgnoreCase("list")) {

				Set<String> permissions = config.getPermissions();

				sender.sendMessage(stAvailablePermissions);

				Config.permissionInfo info;

				for (Object o : permissions) {
					String e = o.toString();
					// sender.sendMessage("  " + e);

					try {
						info = Config.getPermissionInfo(e);

						String node = ChatColor.WHITE + info.name;

						String price = "$"+Integer.toString(info.price);

						String duration = "";
						
						if (info.duration > 0){
							duration = " " + database.secondsToString(info.duration*60);
						}
						
						String uses = "";
						if (info.uses > 0){
							uses = " x" + info.uses;
						}
						
						
						// if (sender.hasPermission(info.node)) {
						// node = ChatColor.GRAY + info.name;
						// }

						ChatColor msgColour = ChatColor.GRAY;
						
						if (player != null) {
							if (playerHasPermissions(player, info.node)) {
							//	node = ChatColor.DARK_GRAY + info.name;
								//price = ChatColor.DARK_GRAY + "$"+Integer.toString(info.price);
								
								msgColour = ChatColor.DARK_GRAY;
							} else if (getBalance(sender.getName()) >= info.price) {

								//node = ChatColor.GREEN + info.name;
								//price = ChatColor.GREEN + "$"+Integer.toString(info.price);
								msgColour = ChatColor.GREEN;
							} else if (getBalance(sender.getName()) < info.price) {
								//node = ChatColor.RED + info.name;
								//price = ChatColor.RED + "$"+Integer.toString(info.price);
								msgColour = ChatColor.RED;
							}
						}

						sender.sendMessage("  " + node + msgColour + uses + " " + price + duration);

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				if (args.length == 1) {

					sender.sendMessage(stIncludePermissionName);
					return true;
				}

				Config.permissionInfo info;
				try {
					info = Config.getPermissionInfo(args[1]);
					if (info != null) {

						sender.sendMessage(stPermissionInfo.format(stPermissionInfo, args[1]));
						sender.sendMessage(stPermissionInfoNode.format(stPermissionInfoNode, info.node));
						sender.sendMessage(stPermissionInfoCommand.format(stPermissionInfoCommand, info.command));
						sender.sendMessage(stPermissionInfoPrice.format(stPermissionInfoPrice, info.price));

						if (info.duration > 0) {
							sender.sendMessage(stPermissionInfoDuration.format(stPermissionInfoDuration, database.secondsToString(info.duration * 60)));

						} else {
							sender.sendMessage(stPermissionInfoDuration.format(stPermissionInfoDuration, "Unlimited"));
						}

						if (info.uses > 0) {
							sender.sendMessage(stPermissionInfoDuration.format(stPermissionInfoUse, info.uses));

						} else {
							sender.sendMessage(stPermissionInfoDuration.format(stPermissionInfoUse, "Unlimited"));
						}

						// sender.sendMessage("  You have permission: " +
						// database.permissionInDB(player.getName().toString(),
						// args[1].toString()));

					} else {

						sender.sendMessage(stPermNoExist.format(args[1]));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;

			} else if (args[0].equalsIgnoreCase("load")) {

				log.info("Load: " + player.getName());
				// PB.unloadPlayerPermissions(player.getName());
				try {

					database.removeActivePermissions(player.getName());
					database.retrieveActivePermissions(player.getName());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// PB.loadPlayerPermissions(player.getName());

				return true;
			} else if (args[0].equalsIgnoreCase("active")) {
				database.showActivePermissions(player);
				return true;
			} else if (args[0].equalsIgnoreCase("reload")) {

				if (sender.isOp()) {
					this.reloadConfig();

					sender.sendMessage(chatPrefix + " plugin reloaded.");
					return true;
				}

				// database.showActivePermissions(player);
			} else if (args[0].equalsIgnoreCase("create") && player.hasPermission(perm_create)) {
				// purchasepermissions.create
				if (args.length == 1) {

					sender.sendMessage(stIncludeName);
					return true;
				}

				log.info(chatPrefix + " create 1");

				if (config.createPermission(player, args[1])) {
					sender.sendMessage(stPermCreated);
				}

				return true;
			} else if (args[0].equalsIgnoreCase("modify") && player.hasPermission(perm_modify)) {
				if (args.length == 1) {

					sender.sendMessage(stAvailableSettings);
					sender.sendMessage(stAvailableSettingsNode);
					sender.sendMessage(stAvailableSettingsPrice);
					sender.sendMessage(stAvailableSettingsDuration);
					sender.sendMessage(stAvailableSettingsCommand);
					sender.sendMessage(stAvailableSettingsUses);
					sender.sendMessage(stAvailableSettingsPayto);
					return true;
				}

				// SQL = String.format(SQL, Config.DbTable);

				if (!config.permissionExists(args[1].toString())) {
					sender.sendMessage(stPermNoExists.format(stPermNoExists, args[1].toString()));
					return true;
				}

				if (args.length == 2) {
					sender.sendMessage(stIncludeSettingName);
					return true;
				}

				if (config.isValidSetting(args[2].toString()) == false) {
					sender.sendMessage(stNotValidSetting.format(stNotValidSetting, args[2].toString()));
					return true;
				}

				if (args.length == 3) {

					sender.sendMessage(stIncludeSettingPerm.format(stIncludeSettingPerm, args[2]));
					return true;
				}

				if (config.modifyPermissionSetting(player, args[1], args[2], args[3])) {
					sender.sendMessage(String.format(stPermModifed, args[1], args[2], args[3]));
				}

				return true;

			} else if (args[0].equalsIgnoreCase("remove") && player.hasPermission(perm_remove)) {
				if (args.length == 1) {
					player.sendMessage(stIncludeName);
					return true;
				}

				if (!config.permissionExists(args[1].toString())) {
					sender.sendMessage(stPermNoExists.format(stPermNoExists, args[1].toString()));
					return true;
				}
				//sender.sendMessage(stPermNoExists.format(stPermNoExists, args[1].toString()));
				
				
				if (config.removePermission(player, args[1])){
					sender.sendMessage(stPermRemoved.format(stPermRemoved, args[1].toString()));
					return true;
				}
				
				
				
			} else if (args[0].equalsIgnoreCase("buy")) {
				if (args.length == 1) {

					sender.sendMessage(stIncludePermName);
					return true;
				}

				Config.permissionInfo info;
				try {
					info = Config.getPermissionInfo(args[1]);
					if (info != null) {

						if (database.permissionInDB(player.getName().toString(), args[1].toString())) {

							sender.sendMessage(stAlreadyOwnPerm);
							return true;
						}

						if (getBalance(sender.getName()) > info.price) {

							String target = player.getName();

							if (args.length == 3) {
								target = args[2];

								log.info(stUserBoughtPerm.format(stUserBoughtPerm, sender.getName(), info.name, target));
							}

							database.removeActivePermissions(sender.getName());
							if (database.addPlayer(target, info)) {
								econ.withdrawPlayer(sender.getName(), info.price);

								String payTo = Config.getPermisionPayTo(info.name);

								// log.info("payto: " + payTo);
								if (payTo != null) {
									if (payPlayer(payTo, info.price)) {

										sender.sendMessage(stPaidUser.format(stPaidUser, payTo, info.price));
									}
								}

								sender.sendMessage(stPurchasedPerm.format(stPurchasedPerm, info.name, target));

								notifyUsersOfPurchase(player, info.name, target);

							} else {

								sender.sendMessage(stFailedToBuyPerm.format(stFailedToBuyPerm, info.name));
							}
							database.retrieveActivePermissions(sender.getName());
							// pb.loadPlayerPermissions(sender.getName());

							// econ.withdrawPlayer(sender.getName(), info.price

						} else {

							sender.sendMessage(stNotEnoughFunds.format(stNotEnoughFunds, info.price));
						}
						// econ.withdrawPlayer(sender.getName(), info.price);

					} else {

						sender.sendMessage(stPermNoExist.format(stPermNoExist, args[1]));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;

			} else if (args[0].equalsIgnoreCase("test")) {
				// sender.sendMessage("currentTimeMillis: " +
				// System.currentTimeMillis());
				// long unixTime = System.currentTimeMillis() / 1000L;
				// sender.sendMessage("unixTime: " + unixTime);

				// Config.testList();

				String node = "time";

				if (args.length == 2) {
					node = args[1];
				}

				Config.permissionInfo info;
				try {
					info = Config.getPermissionInfo(node);
					for (String permissionName : info.node) {
						log.info(node + permissionName);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return true;
			}

		}

		return false;
	}

	public boolean playerHasPermissions(Player player, List<String> nodes) {

		for (String permissionName : nodes) {
			if (!player.hasPermission(permissionName)) {
				return false;
			}
		}

		return true;
	}

	public static String getFinalArg(final String[] args, final int start) {
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public static String resourceToString(String name) {

		InputStream input = PurchasePermissions.class.getResourceAsStream("/" + name);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		if (input != null) {
			try {
				int n;
				Reader reader = new BufferedReader(new InputStreamReader(input));
				while ((n = reader.read(buffer)) != -1)
					writer.write(buffer, 0, n);
			} catch (IOException e) {
				try {
					input.close();
				} catch (IOException ex) {
				}
				return null;
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		} else {
			return null;
		}

		String text = writer.toString().trim();
		text = text.replace("\r\n", " ").replace("\n", " ");
		return text.trim();
	}
}
