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

	public static String chatPrefix = "[PP] ";
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
		chatPrefix = ChatColor.WHITE + "[" + ChatColor.GREEN + "PP" + ChatColor.WHITE + "] ";

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

		log.info(chatPrefix + name + " v" + version + " is enabled.");
	}

	protected void registerPlayer(Player player) {
		log.info(chatPrefix + "Registering " + player.getName());
		if (permissions.containsKey(player.getName())) {
			log.info("Registering " + player.getName() + ": was already registered");
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
		log.info(chatPrefix + "Unegistering " + player.getName());
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

	public void notifyUsersOfPurchase(Player sender, String permissionName) {
		for (Player oPlayer : getServer().getOnlinePlayers()) {
			// if (sender != oPlayer) {
			if (oPlayer.hasPermission(Config.notifyPurchase)) {
				oPlayer.sendMessage(chatPrefix + ChatColor.GREEN + sender.getDisplayName() + ChatColor.WHITE + " has purchased " + ChatColor.GREEN
					+ permissionName + ChatColor.WHITE + ".");
			} else {
				log.info(oPlayer.getName() + " does not have access to " + Config.notifyPurchase);

			}
			// }
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
				sender.sendMessage(chatPrefix + "/pp list - List all permissions.");
				sender.sendMessage(chatPrefix + "/pp info <permission> - Get info on a permission.");
				sender.sendMessage(chatPrefix + "/pp buy <permission> " + ChatColor.GRAY + "[player]" + ChatColor.WHITE + " - Buy a permission.");
				sender.sendMessage(chatPrefix + "/pp active - Display your active permissions.");

				return true;
			}

			if (args[0].equalsIgnoreCase("list")) {

				Set<String> permissions = config.getPermissions();

				sender.sendMessage(chatPrefix + " Available permissions");

				Config.permissionInfo info;

				for (Object o : permissions) {
					String e = o.toString();
					// sender.sendMessage("  " + e);

					try {
						info = Config.getPermissionInfo(e);

						String node = ChatColor.WHITE + info.name;

						String price = Integer.toString(info.price);

						// if (sender.hasPermission(info.node)) {
						// node = ChatColor.GRAY + info.name;
						// }

						if (player != null) {
							if (playerHasPermissions(player, info.node)) {
								node = ChatColor.DARK_GRAY + info.name;
							} else if (getBalance(sender.getName()) >= info.price) {

								node = ChatColor.GREEN + info.name;
								price = ChatColor.GREEN + Integer.toString(info.price);

							} else if (getBalance(sender.getName()) < info.price) {
								node = ChatColor.RED + info.name;
								price = ChatColor.RED + Integer.toString(info.price);
							}
						}

						sender.sendMessage("  " + node + " $" + price);

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				if (args.length == 1) {
					sender.sendMessage(chatPrefix + " You need to include the permission's name.");
					return true;
				}

				Config.permissionInfo info;
				try {
					info = Config.getPermissionInfo(args[1]);
					if (info != null) {
						sender.sendMessage(chatPrefix + args[1] + " Info.");
						sender.sendMessage("  node: " + info.node);
						sender.sendMessage("  command: " + info.command);
						sender.sendMessage("  price: $" + info.price);

						if (info.duration > 0){
							sender.sendMessage("  duration: " + ChatColor.GREEN + database.secondsToString(info.duration * 60));
						}else{
							sender.sendMessage("  duration: " + ChatColor.GREEN + "Unlimited");
						}
						
						if (info.uses > 0 ){
							sender.sendMessage("  uses: " + ChatColor.GREEN + info.uses);
						}else{
							sender.sendMessage("  uses: " + ChatColor.GREEN + "Unlimited");
						}
						

						// sender.sendMessage("  You have permission: " +
						// database.permissionInDB(player.getName().toString(),
						// args[1].toString()));

					} else {
						sender.sendMessage(chatPrefix + "Permission '" + args[1] + "' does not exist, try again.");
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

			} else if (args[0].equalsIgnoreCase("buy")) {
				if (args.length == 1) {
					sender.sendMessage(chatPrefix + " You need to include the permission's name.");
					return true;
				}

				Config.permissionInfo info;
				try {
					info = Config.getPermissionInfo(args[1]);
					if (info != null) {

						if (database.permissionInDB(player.getName().toString(), args[1].toString())) {
							sender.sendMessage(chatPrefix + " You already own that permission.");
							return true;
						}

						if (getBalance(sender.getName()) > info.price) {

							String target = player.getName();

							if (args.length == 3) {
								target = args[2];

								log.info(chatPrefix + sender.getName() + " bought " + info.name + " for " + target + ".");
							}

							database.removeActivePermissions(sender.getName());
							if (database.addPlayer(target, info)) {
								econ.withdrawPlayer(sender.getName(), info.price);

								String payTo = Config.getPermisionPayTo(info.name);

								// log.info("payto: " + payTo);
								if (payTo != null) {
									if (payPlayer(payTo, info.price)) {
										sender.sendMessage(chatPrefix + "Paid " + payTo + " $" + info.price + ".");
									}
								}

								// payPlayer

								sender.sendMessage(chatPrefix + info.name + " purchased for " + target + ".");

								notifyUsersOfPurchase(player, info.name);

							} else {
								sender.sendMessage(chatPrefix + " failed to buy permission.");
							}
							database.retrieveActivePermissions(sender.getName());
							// pb.loadPlayerPermissions(sender.getName());

							// econ.withdrawPlayer(sender.getName(), info.price

						} else {
							sender.sendMessage(chatPrefix + " You don't have enought funds to buy that permission.");
						}
						// econ.withdrawPlayer(sender.getName(), info.price);

					} else {
						sender.sendMessage(chatPrefix + "Permission '" + args[1] + "' does not exist, try again.");
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
