package com.cyprias.purchasepermissions;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Database {

	private static String chatPrefix = "§f[§aPP§f] ";
	private String stCannotUsePermInWorld = chatPrefix+"§7You cannot use §f%s §7in that world.";
	
	
	static int col_id = 1;
	static int col_player = 2;
	static int col_permission = 3;
	static int col_remainingUses = 4;
	static int col_expires = 5;

	Logger log = Logger.getLogger("Minecraft");
	
	private PurchasePermissions plugin;

	
	
	public Database(PurchasePermissions plugin) {
		this.plugin = plugin;
	}
		

	
	public void retrieveActivePermissions(Player player) throws SQLException {

		String playerName = player.getName();

		String SQL = "SELECT * FROM `" + Config.DbTable + "` WHERE `player` = '" + playerName + "'";

		// log.info("retrieveActivePermissions: " + SQL);

		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement(SQL);
		ResultSet result = statement.executeQuery();

		while (result.next()) {
			if (PurchasePermissions.permissions.containsKey(result.getString(col_player))) {
				if (plugin.config.canUsePermissionInWorld(player, result.getString(col_permission)) == true) {
					PlayerListener.addPermission(result.getString(col_player), result.getString(col_permission));
				}
				
			}
		}

		statement.close();
		con.close();
	}

	public void removeActivePermissions(String playerName) throws SQLException {

		String SQL = "SELECT * FROM `" + Config.DbTable + "` WHERE `player` = '" + playerName + "'";

		// log.info("retrieveActivePermissions: " + SQL);

		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement(SQL);
		ResultSet result = statement.executeQuery();

		// log.info("retrieveActivePermissions: 2");
		while (result.next()) {
			if (PurchasePermissions.permissions.containsKey(result.getString(col_player))) {
				PlayerListener.removePermission(result.getString(col_player), result.getString(col_permission));
			}
		}

		statement.close();
		con.close();
	}

	public static long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public boolean addPlayer(String playerName, Config.permissionInfo pInfo) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");

		double expires = 0;

		if (pInfo.duration > 0) {
			expires = getUnixTime() + (pInfo.duration * 60);
		}

		String SQL = "INSERT INTO `" + Config.DbTable + "` (`id`, `player`, `permission`, `remainingUses`, `expires`) " + "VALUES (NULL, '" + playerName
			+ "', '" + pInfo.name + "', '" + pInfo.uses + "', '" + expires + "');";

		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement(SQL);

		try {
			int result = statement.executeUpdate();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public String secondsToString(long totalSeconds) {
		long hours = totalSeconds / 3600;
		long remainder = totalSeconds % 3600, minutes = remainder / 60, seconds = remainder % 60;
		return hours + "h" + minutes + "m" + seconds + "s";
	}

	public static void showActivePermissions(Player player) {

		try {
			String SQL = "select * from " + Config.DbTable + " WHERE player = '" + player.getName().toString() + "'";
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
			PreparedStatement statement = con.prepareStatement(SQL);
			ResultSet result = statement.executeQuery();

			// int uses = result.getInt(col_remainingUses);
			// int pID = result.getInt(col_id);

			String pMsg = null;
			long currentTime = getUnixTime();
			int i = 0;
			while (result.next()) {
				i = i + 1;

				pMsg = PurchasePermissions.chatPrefix + i + ": Permission: " + ChatColor.GREEN + result.getString(col_permission);

				if (result.getInt(col_remainingUses) > 0) {
					pMsg = pMsg + ChatColor.WHITE + ", Remaining uses: " + ChatColor.GREEN + result.getInt(col_remainingUses);
				}

				if (result.getInt(col_expires) > 0) {

					long expires = result.getLong(col_expires);
					long remaining = expires - currentTime;

					long hours = remaining / 3600;
					long remainder = remaining % 3600, minutes = remainder / 60, seconds = remainder % 60;

					// String disHour = (hours < 10 ? "0" : "") + hours,
					// disMinu = (minutes < 10 ? "0" : "") + minutes ,
					// disSec = (seconds < 10 ? "0" : "") + seconds ;

					pMsg = pMsg + ChatColor.WHITE + ", Remaining time: " + ChatColor.GREEN + hours + "h " + minutes + "m " + seconds + "s";

				}

				player.sendMessage(pMsg);

			}
			if (i == 0) {
				player.sendMessage(PurchasePermissions.chatPrefix + "You have no purchased permisions.");
			}

			result.close();
			statement.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	public void commandUsed(Player player, String message) throws Exception {
		// log.info("commandUsed: " + playerName + " " + message);

		String playerName = player.getName();
		
		//if Config.canUsePermissionInWorld(event.getPlayer(), )
		
		Set<String> permissions = plugin.config.getPermissions();
		
		Config.permissionInfo pInfo = plugin.config.isValidCommand(message);
			
		if (pInfo == null)
			return;
		
		
		
		//pInfo.
		
		if (isPermissionActive(player.getName(), pInfo.name) && plugin.config.canUsePermissionInWorld(player, pInfo.name) == false){ //We don't permit user to use command in that world.
			
			if (! plugin.playerHasPermissions(player, pInfo.node)) {//Make sure other permission plugins don't either before notifying user.
				player.sendMessage(stCannotUsePermInWorld.format(stCannotUsePermInWorld, pInfo.name));
			}
			return;
		}
			
	
		//log.info("commandUsed: " + pInfo.name);
		
		String SQL = "select * from " + Config.DbTable + 
		" WHERE player = '" + playerName.toString() + "'" + 
		" AND permission = '" + pInfo.name + "'";
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement(SQL);
		ResultSet result = statement.executeQuery();
		
		while (result.next()) {
			// String name = result.getString(col_player);
			int uses = result.getInt(col_remainingUses);
			int pID = result.getInt(col_id);
			
			//log.info("commandUsed result: " + pID + " " + uses);
			
			if (uses == 1) {

				if (removePermissionFromDB(pID)) {
					Player user = PurchasePermissions.server.getPlayer(playerName);

					PlayerListener.removePermission(playerName, pInfo.name);

					if (user != null) {
						user.sendMessage(PurchasePermissions.chatPrefix + "Your " + ChatColor.GREEN + pInfo.name + ChatColor.WHITE
							+ " permission has expired.");
					}

				}
			} else if (uses > 1) {

				// UPDATE `minecraft`.`pp_players` SET `remainingUses` = '2'
				// WHERE `pp_players`.`id` =33;

				updatePermissionUses(pID, uses - 1);

			}
		}
		
		
		result.close();
		statement.close();
		con.close();
		
		/*
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement("select * from " + Config.DbTable + " WHERE player = '" + playerName.toString() + "'");
		ResultSet result = statement.executeQuery();

		while (result.next()) {
			// String name = result.getString(col_player);
			String permissionName = result.getString(col_permission);

			String command = Config.getPermisionCommand(permissionName);

			// log.info("Active: " + playerName + " " + permissionName + " /" +
			// command);
			if (command != null && message.contains("/" + command)) {
				// log.info(playerName + " used their " + permissionName +
				// " permission!");

				int uses = result.getInt(col_remainingUses);
				int pID = result.getInt(col_id);

				if (uses == 1) {

					if (removePermissionFromDB(pID)) {
						Player user = PurchasePermissions.server.getPlayer(playerName);

						PlayerListener.removePermission(playerName, permissionName);

						if (user != null) {
							user.sendMessage(PurchasePermissions.chatPrefix + "Your " + ChatColor.GREEN + permissionName + ChatColor.WHITE
								+ " permission has expired.");
						}

					}
				} else if (uses > 1) {

					// UPDATE `minecraft`.`pp_players` SET `remainingUses` = '2'
					// WHERE `pp_players`.`id` =33;

					updatePermissionUses(pID, uses - 1);

				}

			}

		}

		result.close();
		statement.close();
		con.close();
		*/
	}



	public boolean isPermissionActive(String playerName, String permissionName) throws Exception {
		boolean found = false;

		String SQL = "select * from " + Config.DbTable.toString() + " WHERE player = '" + playerName + "' AND permission = '" + permissionName
			+ "' LIMIT 0 , 5";

		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement(SQL);
		ResultSet result = statement.executeQuery();

		while (result.next()) {
			// result.getInt(1);

			int id = result.getInt(col_id);
			String name = result.getString(col_player);
			String permission = result.getString(col_permission);

			//log.info("isPermissionActive " + id);
			// return true
			found = true;
			break;
		}

		result.close();
		statement.close();
		con.close();
		return found;
	}

	public void cleanOldEntries() throws Exception {
		// log.info(PurchasePermissions.chatPrefix + "cleanOldEntries");

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement("select * from " + Config.DbTable);
		ResultSet result = statement.executeQuery();

		long currentTime = getUnixTime();

		while (result.next()) {
			// result.getInt(1);

			String name = result.getString(col_player);
			String permission = result.getString(col_permission);
			long expires = result.getLong(col_expires);

			if (expires > 0) {
				long remaining = expires - currentTime;

				if (remaining < 0) {

					Player playerObj = Bukkit.getServer().getPlayer(name);
					if (playerObj != null) {
						playerObj.sendMessage(PurchasePermissions.chatPrefix + " Your " + ChatColor.GREEN + permission + ChatColor.WHITE
							+ " permission has expired.");
					}
					log.info(PurchasePermissions.chatPrefix + "Removing " + name + "'s " + permission + ".");

					// pb.unloadPlayerPermissions(name);
					// removeActivePermissions(name);

					removePermissionFromDB(result.getInt(col_id));

					PlayerListener.removePermission(name, permission);

					// pb.loadPlayerPermissions(name);
					// retrieveActivePermissions(name);

				}
			}

			// log.info(PurchasePermissions.chatPrefix +
			// "---------------------------");
			// log.info(PurchasePermissions.chatPrefix + "db_id: " +
			// result.getString(db_id));
		}

		result.close();
		statement.close();
		con.close();
	}

	public static void updatePermissionUses(int pID, int uses) {

		try {
			Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
			PreparedStatement statement = con.prepareStatement("UPDATE " + Config.DbTable + " SET remainingUses = '" + uses + "' WHERE id =" + pID + ";");
			statement.executeUpdate();
			statement.close();
			con.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean removePermissionFromDB(int ID) throws Exception {
		try {
			Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
			PreparedStatement statement = con.prepareStatement("DELETE FROM `" + Config.DbTable + "` WHERE `id` = '" + ID + "'");
			statement.executeUpdate();
			statement.close();
			con.close();

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public void setupMysql() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(Config.DbUrl, Config.DbUser, Config.DbPassword);
		PreparedStatement statement = con.prepareStatement("show tables like '%" + Config.DbTable + "%'");

		ResultSet result = statement.executeQuery();

		result.last();
		if (result.getRow() == 0) {
			log.info(PurchasePermissions.chatPrefix + "Creating our MySQL table.");

			String SQL = PurchasePermissions.resourceToString("Create-Table-mysql.sql");
			SQL = String.format(SQL, Config.DbTable);

			try {
				statement = con.prepareStatement(SQL);
				int result2 = statement.executeUpdate();
				log.info(PurchasePermissions.chatPrefix + "result2: " + result2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		result.close();
		statement.close();
		con.close();
	}
}
