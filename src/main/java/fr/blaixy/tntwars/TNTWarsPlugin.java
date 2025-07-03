package fr.blaixy.tntwars;

import fr.blaixy.tntwars.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TNTWarsPlugin extends JavaPlugin {

    private static TNTWarsPlugin instance;
    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;
    private LocationManager locationManager;
    private MapManager mapManager;

    @Override
    public void onEnable() {
        instance = this;

        // Configuration par défaut
        setupDefaultConfig();

        // Initialisation des managers
        this.gameManager = new GameManager(this);
        this.scoreboardManager = new ScoreboardManager();
        this.locationManager = new LocationManager();
        this.mapManager = new MapManager(this);

        // Enregistrement des events
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        // Enregistrement des commandes
        getCommand("tntwars").setExecutor(new CommandManager(this));

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
        // Créer le fichier de configuration par défaut s'il n'existe pas
        try {
            saveDefaultConfig();
        } catch (IllegalArgumentException e) {
            // Si le fichier config.yml n'existe pas dans les ressources, on le crée manuellement
            getLogger().warning("Fichier config.yml par défaut introuvable, création manuelle...");
            createDefaultConfig();
        }

        // Configuration des locations par défaut si elles n'existent pas
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

    private void createDefaultConfig() {
        // Créer la configuration par défaut programmatiquement
        getConfig().set("game.countdown-time", 20);
        getConfig().set("game.max-team-size", 2);
        getConfig().set("game.end-game-delay", 5);
        getConfig().set("game.tnt-damage-multiplier", 2.0);
        getConfig().set("game.tnt-explosion-power", 0.5);

        getConfig().set("messages.prefix", "§c§l[TNT Wars]§r ");
        getConfig().set("messages.welcome", "§a§lBienvenue dans TNT Wars !");
        getConfig().set("messages.team-selection", "§e§lCliquez sur la laine pour choisir votre équipe !");

        getConfig().set("items.tnt-amount", 16);
        getConfig().set("items.cobblestone-amount", 64);
        getConfig().set("items.redstone-amount", 16);
        getConfig().set("items.repeater-amount", 8);

        getConfig().set("blocks.max-build-height", 150);

        getConfig().set("scoreboard.title", "§c§l⚡ TNT WARS ⚡");
        getConfig().set("scoreboard.server-ip", "play.tntwars.fr");

        getConfig().set("debug.enabled", false);
        getConfig().set("debug.log-game-events", true);

        saveConfig();
        getLogger().info("Configuration par défaut créée !");
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

    public MapManager getMapManager() {
        return mapManager;
    }

    public void setMapManager(MapManager mapManager) {
        this.mapManager = mapManager;
    }
}