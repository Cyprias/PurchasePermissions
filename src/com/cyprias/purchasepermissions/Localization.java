package com.cyprias.purchasepermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Localization {
	File localesFile = null;
	FileConfiguration locales = new YamlConfiguration();

	Logger log = Logger.getLogger("Minecraft");

	private PurchasePermissions plugin;

	public HashMap<String, String> L = new HashMap<String, String>();
	
	public Localization(PurchasePermissions plugin) {
		this.plugin = plugin;

		loadLocales();
	}

	private void reloadLocalesFile() {
		try {
			localesFile = new File(plugin.getDataFolder(), "locales.yml");

			// Check if file exists in plugin dir, or create it.
			if (!localesFile.exists()) {
				localesFile.getParentFile().mkdirs();
				copy(plugin.getResource("locales.yml"), localesFile);

			}

			locales.load(localesFile);

			loadDefaultLocales(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void copyDefaultValue(String language, String key, String value) {
		log.info("[PP] Loading default for " + language + "'s " + key + " value: " + value);
		locales.getConfigurationSection(language).set(key, value);
	}

	public void loadDefaultLocales(boolean forceLoad) {
		InputStream defConfigStream = plugin.getResource("locales.yml");
		// log.info("loadDefaultLocales 2: " + defConfigStream);

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			// log.info("loadDefaultLocales 3: " + defConfig);

			Set<String> languages = defConfig.getKeys(false); // locales.getConfigurationSection("enUS").getKeys(false);

			Set<String> languageSection;
			String value;
			boolean saveLocales = false;
			boolean warnedUsers = false;
			for (String language : languages) {
				// log.info("loadDefaultLocales 2 " + language);

				if (!locales.isSet(language)) {
					log.info("[PP] Creating " + language + " section in our locales.");
					locales.createSection(language);
					saveLocales = true;
				}

				for (String key : defConfig.getConfigurationSection(language).getKeys(false)) {
					// log.info("loadDefaultLocales 3 " + key);
					value = defConfig.getConfigurationSection(language).getString(key);
					// log.info("loadDefaultLocales 4 " + value);
					//
					// log.info("loadDefaultLocales 2 " + language + " " + key +
					// " " + value);

					/**/
					if (forceLoad == true || !locales.getConfigurationSection(language).isSet(key)) {
						copyDefaultValue(language, key, value);
						saveLocales = true;
					} else if (!value.equalsIgnoreCase(locales.getConfigurationSection(language).getString(key))) {

						if (Config.autoLoadDefaultLocales == true) {
							copyDefaultValue(language, key, value);
							saveLocales = true;
						} else {
							if (warnedUsers == false)
								log.info("[PP] Language defaults have changed, use /pp locale to load them.");
							warnedUsers = true;
						}
					}

				}
			}
			if (saveLocales == true)
				try {
					log.info("[PP] Saving locales file.");
					locales.save(localesFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	private void copy(InputStream in, File file) {
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

	

	public void loadLocales() {
		if (localesFile == null) {
			reloadLocalesFile();
		}

		log.info("loadLocales locale: " + Config.locale);

		String value;
		for (String key : locales.getConfigurationSection(Config.locale).getKeys(false)) {
			value = locales.getConfigurationSection(Config.locale).getString(key);
			L.put(key, value.replaceAll("(?i)&([a-k0-9])", "\u00A7$1"));

			//log.info("loadLocales " + key + " " + L.get(key));
		}

	}

	public void listLocales() {
		if (localesFile == null) {
			reloadLocalesFile();
		}

		// log.info("listLocales");

		Set<String> languages = locales.getKeys(false); // locales.getConfigurationSection("enUS").getKeys(false);

		Set<String> languageSection;
		String value;
		for (String language : languages) {
			// log.info("loadDefaultLocales 2 " + language);

			for (String key : locales.getConfigurationSection(language).getKeys(false)) {
				// log.info("loadDefaultLocales 3 " + key);
				value = locales.getConfigurationSection(language).getString(key);
				// log.info("loadDefaultLocales 4 " + value);
				log.info("listLocales 1 " + language + " " + key + " " + value);

			}

		}

	}
}
