package fr.blaixy.tntwars;

import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MapManager {

    private TNTWarsPlugin plugin;
    private Map<String, Material> originalBlocks;
    private Location arenaMin;
    private Location arenaMax;
    private boolean isRecording;

    public MapManager(TNTWarsPlugin plugin) {
        this.plugin = plugin;
        this.originalBlocks = new HashMap<>();
        this.isRecording = false;
        setupArenaRegion();
    }

    private void setupArenaRegion() {
        // Définir la région de l'arène basée sur les spawns
        Location redSpawn = plugin.getLocationManager().getRedSpawn();
        Location blueSpawn = plugin.getLocationManager().getBlueSpawn();

        if (redSpawn != null && blueSpawn != null) {
            World world = redSpawn.getWorld();

            // Calculer les coordonnées min et max de l'arène
            double minX = Math.min(redSpawn.getX(), blueSpawn.getX()) - 50;
            double maxX = Math.max(redSpawn.getX(), blueSpawn.getX()) + 50;
            double minY = Math.min(redSpawn.getY(), blueSpawn.getY()) - 20;
            double maxY = Math.max(redSpawn.getY(), blueSpawn.getY()) + 30;
            double minZ = Math.min(redSpawn.getZ(), blueSpawn.getZ()) - 50;
            double maxZ = Math.max(redSpawn.getZ(), blueSpawn.getZ()) + 50;

            arenaMin = new Location(world, minX, minY, minZ);
            arenaMax = new Location(world, maxX, maxY, maxZ);
        } else {
            // Valeurs par défaut si pas de spawns définis
            World world = Bukkit.getWorld("world");
            if (world != null) {
                arenaMin = new Location(world, -100, 50, -100);
                arenaMax = new Location(world, 100, 200, 100);
            }
        }
    }

    public void startRecording() {
        if (arenaMin == null || arenaMax == null) {
            plugin.getLogger().warning("Impossible de démarrer l'enregistrement: région d'arène non définie!");
            return;
        }

        isRecording = true;
        originalBlocks.clear();

        // Sauvegarder l'état actuel des blocs dans la zone d'arène
        new BukkitRunnable() {
            @Override
            public void run() {
                recordArenaBlocks();
                isRecording = false;
                plugin.getLogger().info("Enregistrement de la map terminé! " + originalBlocks.size() + " blocs sauvegardés.");
            }
        }.runTaskAsynchronously(plugin);
    }

    private void recordArenaBlocks() {
        World world = arenaMin.getWorld();

        for (int x = arenaMin.getBlockX(); x <= arenaMax.getBlockX(); x++) {
            for (int y = arenaMin.getBlockY(); y <= arenaMax.getBlockY(); y++) {
                for (int z = arenaMin.getBlockZ(); z <= arenaMax.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    String key = x + "," + y + "," + z;
                    originalBlocks.put(key, block.getType());
                }
            }
        }
    }

    public void resetMap() {
        if (originalBlocks.isEmpty()) {
            plugin.getLogger().warning("Aucune sauvegarde de map trouvée! Enregistrement automatique...");
            startRecording();
            return;
        }

        // Réinitialiser les blocs de manière asynchrone pour éviter les lags
        new BukkitRunnable() {
            int processed = 0;

            @Override
            public void run() {
                World world = arenaMin.getWorld();

                for (Map.Entry<String, Material> entry : originalBlocks.entrySet()) {
                    if (processed >= 1000) {
                        // Traiter par batch de 1000 blocs par tick
                        return;
                    }

                    String[] coords = entry.getKey().split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);

                    Block block = world.getBlockAt(x, y, z);
                    Material originalType = entry.getValue();

                    if (block.getType() != originalType) {
                        block.setType(originalType);
                    }

                    processed++;
                }

                if (processed >= originalBlocks.size()) {
                    cancel();
                    plugin.getLogger().info("Réinitialisation de la map terminée!");

                    // Nettoyer les items au sol
                    clearItemsInArena();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void clearItemsInArena() {
        World world = arenaMin.getWorld();

        world.getEntities().forEach(entity -> {
            Location loc = entity.getLocation();
            if (isInArena(loc)) {
                if (entity.getType().name().equals("DROPPED_ITEM") ||
                        entity.getType().name().equals("PRIMED_TNT")) {
                    entity.remove();
                }
            }
        });
    }

    public boolean isInArena(Location location) {
        if (arenaMin == null || arenaMax == null) {
            return false;
        }

        return location.getWorld().equals(arenaMin.getWorld()) &&
                location.getX() >= arenaMin.getX() && location.getX() <= arenaMax.getX() &&
                location.getY() >= arenaMin.getY() && location.getY() <= arenaMax.getY() &&
                location.getZ() >= arenaMin.getZ() && location.getZ() <= arenaMax.getZ();
    }

    public void setArenaRegion(Location pos1, Location pos2) {
        if (pos1.getWorld().equals(pos2.getWorld())) {
            double minX = Math.min(pos1.getX(), pos2.getX());
            double maxX = Math.max(pos1.getX(), pos2.getX());
            double minY = Math.min(pos1.getY(), pos2.getY());
            double maxY = Math.max(pos1.getY(), pos2.getY());
            double minZ = Math.min(pos1.getZ(), pos2.getZ());
            double maxZ = Math.max(pos1.getZ(), pos2.getZ());

            arenaMin = new Location(pos1.getWorld(), minX, minY, minZ);
            arenaMax = new Location(pos1.getWorld(), maxX, maxY, maxZ);

            plugin.getLogger().info("Région d'arène définie de " + arenaMin + " à " + arenaMax);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public int getSavedBlocksCount() {
        return originalBlocks.size();
    }
}