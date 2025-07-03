package fr.blaixy.tntwars;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scoreboard.ScoreboardManager;

public class TNTWarsPlugin extends JavaPlugin {

    private static TNTWarsPlugin instance;
    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;
    private LocationManager locationManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation des managers
        this.gameManager = new GameManager(this);
        this.scoreboardManager = new ScoreboardManager();
        this.locationManager = new LocationManager();

        // Enregistrement des events
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        // Enregistrement des commandes
        getCommand("tntwars").setExecutor(new CommandManager(this));

        // Configuration par défaut
        setupDefaultConfig();

        getLogger().info("TNT Wars Plugin activé !");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.endGame();
        }
        getLogger().info("TNT Wars Plugin désactivé !");
    }

    private void setupDefaultConfig() {
        saveDefaultConfig();

        // Configuration des locations par défaut
        if (!getConfig().contains("locations")) {
            World world = Bukkit.getWorld("world");
            if (world != null) {
                getConfig().set("locations.lobby", locationToString(new Location(world, 0, 100, 0)));
                getConfig().set("locations.red-spawn", locationToString(new Location(world, -50, 100, 0)));
                getConfig().set("locations.blue-spawn", locationToString(new Location(world, 50, 100, 0)));
                saveConfig();
            }
        }
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static TNTWarsPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}