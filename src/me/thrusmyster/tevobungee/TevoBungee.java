package me.thrusmyster.tevobungee;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import me.thrusmyster.tevobungee.SQL.SQLManager;
import me.thrusmyster.tevobungee.Voting.MainVoteListener;
import me.thrusmyster.tevobungee.Voting.VoteListener;
import me.thrusmyster.tevobungee.Voting.VoteManager;
import me.thrusmyster.tevobungee.Voting.VoteReceiver;
import me.thrusmyster.tevobungee.Voting.crypto.RSAIO;
import me.thrusmyster.tevobungee.Voting.crypto.RSAKeygen;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class TevoBungee extends Plugin {

	private Logger logger;
	private ProxyServer proxy;
	private SQLManager sql;
	private SQLManager punish_sql;
	private static TevoBungee instance;
	private KeyPair keypair;
	private final List<VoteListener> listeners = new ArrayList<VoteListener>();
	private VoteReceiver votereceiver;
	private Configuration config;
	private ScheduledTask votetask;

	public void onEnable() {
		instance = this;
		logger = this.getLogger();
		proxy = this.getProxy();
		logger.info("-----------------------------------------");
		logger.info("Tevo Bungee");
		logger.info("Author: Thrusmyster");
		logger.info("Version: " + getDescription().getVersion());
		logger.info("Handles voting, motd, and cross server MSGs.");
		logger.info("Loading Tevo Bungee...");
		logger.info("-----------------------------------------");
		startup();

	}

	public void onDisable() {
		logger.info("Shutting down...");
		if (this.votereceiver != null) {
			this.votereceiver.shutdown();
			this.votetask.cancel();
		}
	}

	public static TevoBungee getInstance() {
		return instance;
	}

	public SQLManager getSQLManager() {
		return this.sql;
	}

	public SQLManager getPunishSQlManager() {
		return this.punish_sql;
	}

	public ProxyServer getProxyServer() {
		return this.proxy;
	}

	public Logger getUtilLogger() {
		return this.logger;
	}

	public KeyPair getKeyPair() {
		return this.keypair;
	}

	public Configuration getConfig() {
		return this.config;
	}

	public List<VoteListener> getListeners() {
		return this.listeners;
	}

	private void registerListeners() {
		logger.info("Registering Listeners...");
		proxy.getPluginManager().registerListener(this, new Listeners());
		logger.info("Registered Listeners!");
	}

	private void registerCMDS() {
		logger.info("Registering Commands...");
		proxy.getPluginManager().registerCommand(this, new TNCMD());
		proxy.getPluginManager().registerCommand(this, new ReportCMD());
		logger.info("Registered Commands!");
	}

	private void registerChannels() {
		this.proxy.registerChannel("TevoNetworkIncoming");
		this.proxy.registerChannel("TevoNetworkOutgoing");
	}

	private void startTasks() {
		this.proxy.getScheduler().schedule(this, this.sql, 30L, 300L, TimeUnit.SECONDS);
		this.proxy.getScheduler().schedule(this, this.punish_sql, 30L, 300L, TimeUnit.SECONDS);
		this.proxy.getScheduler().schedule(this, new VoteManager(), 30L, 60L, TimeUnit.SECONDS);
	}

	private void startup() {
		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		listeners.add(new MainVoteListener());

		File cfile = new File(getDataFolder() + "/config.yml");

		try {
			if (!cfile.exists()) {
				Files.copy(getResourceAsStream("config.yml"), cfile.toPath(), new CopyOption[0]);
			}
			reloadConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		this.sql = new SQLManager();
		this.punish_sql = new SQLManager();
		if ((this.sql.initialize("tevoserv_MC")) && (this.punish_sql.initialize("tevoserv_Punish"))) {
			logger.info("SQL> Startup Successful!");
		}
		else {
			logger.warning("SQL> Failed to startup correctly!");
		}
		if (!getConfig().getBoolean("dev")) {
			File rsadir = new File(this.getDataFolder() + "/rsa");
			String host = "198.20.172.198";
			int port = 8192;
			try {
				if (!rsadir.exists()) {
					rsadir.mkdir();
					this.keypair = RSAKeygen.generate(2048);
					RSAIO.save(rsadir, this.keypair);
				}
				else {
					this.keypair = RSAIO.load(rsadir);
				}
			}
			catch (Exception e) {
				logger.info("RSA> Error starting up RSA keys!");
			}
			try {
				this.votereceiver = new VoteReceiver(host, port);
				this.votetask = getProxy().getScheduler().runAsync(this, this.votereceiver);

				logger.info("Vote> Vote listener started!");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		registerListeners();
		registerCMDS();
		registerChannels();
		startTasks();
	}

	public void reloadConfig() {
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "/config.yml"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
