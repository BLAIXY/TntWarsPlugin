package fr.blaixy.tntwars;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;

public class WorldManager {

    private TNTWarsPlugin plugin;
    private String templateWorldName;
    private String gameWorldName;
    private World gameWorld;

    public WorldManager(TNTWarsPlugin plugin) {
        this.plugin = plugin;
        this.templateWorldName = "tntwars_template"; // Nom du monde template
        this.gameWorldName = "tntwars_game";        // Nom du monde de jeu
        setupWorldConfiguration();
    }

    private void setupWorldConfiguration() {
        // Ajouter la configuration dans le config.yml
        plugin.getConfig().addDefault("world.template-world-name", templateWorldName);
        plugin.getConfig().addDefault("world.game-world-name", gameWorldName);
        plugin.getConfig().addDefault("world.auto-reload-on-restart", true);
        plugin.getConfig().addDefault("world.backup-before-reload", true);
        plugin.saveConfig();

        this.templateWorldName = plugin.getConfig().getString("world.template-world-name", templateWorldName);
        this.gameWorldName = plugin.getConfig().getString("world.game-world-name", gameWorldName);
    }

    /**
     * Initialise le monde de jeu au démarrage du serveur
     */
    public void initializeGameWorld() {
        try {
            // Vérifier si le monde template existe
            if (!isTemplateWorldExists()) {
                plugin.getLogger().severe("Le monde template '" + templateWorldName + "' n'existe pas!");
                plugin.getLogger().severe("Veuillez copier votre map dans le dossier: " + templateWorldName);
                return;
            }

            // Décharger le monde de jeu s'il existe déjà
            if (gameWorld != null) {
                unloadGameWorld();
            }

            // Supprimer l'ancien monde de jeu
            deleteGameWorld();

            // Copier le monde template vers le monde de jeu
            copyTemplateToGameWorld();

            // Charger le nouveau monde de jeu
            loadGameWorld();

            plugin.getLogger().info("Monde de jeu rechargé avec succès depuis le template!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation du monde de jeu:", e);
        }
    }

    /**
     * Recharge la map depuis le template
     */
    public void reloadMapFromTemplate() {
        try {
            // Téléporter tous les joueurs vers le spawn du monde principal
            teleportAllPlayersToMainWorld();

            // Attendre un moment pour que les joueurs soient téléportés
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    initializeGameWorld();

                    // Mettre à jour les locations après le reload
                    updateLocationsAfterReload();

                    plugin.getLogger().info("Map rechargée avec succès!");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Erreur lors du rechargement de la map:", e);
                }
            }, 20L); // Attendre 1 seconde

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du rechargement de la map:", e);
        }
    }

    private boolean isTemplateWorldExists() {
        File templateDir = new File(Bukkit.getWorldContainer(), templateWorldName);
        return templateDir.exists() && templateDir.isDirectory();
    }

    private void unloadGameWorld() {
        if (gameWorld != null) {
            // Téléporter tous les joueurs du monde de jeu
            for (Player player : gameWorld.getPlayers()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }

            // Décharger le monde
            Bukkit.unloadWorld(gameWorld, false);
            gameWorld = null;
        }
    }

    private void deleteGameWorld() {
        File gameWorldDir = new File(Bukkit.getWorldContainer(), gameWorldName);
        if (gameWorldDir.exists()) {
            deleteDirectory(gameWorldDir);
        }
    }

    private void copyTemplateToGameWorld() throws IOException {
        File templateDir = new File(Bukkit.getWorldContainer(), templateWorldName);
        File gameWorldDir = new File(Bukkit.getWorldContainer(), gameWorldName);

        copyDirectory(templateDir, gameWorldDir);

        // Modifier le fichier level.dat pour changer le nom du monde
        updateLevelDat(gameWorldDir);
    }

    private void loadGameWorld() {
        WorldCreator creator = new WorldCreator(gameWorldName);
        gameWorld = creator.createWorld();

        if (gameWorld != null) {
            // Configurer le monde de jeu
            gameWorld.setSpawnFlags(true, true);
            gameWorld.setKeepSpawnInMemory(true);
            gameWorld.setAutoSave(false); // Désactiver la sauvegarde automatique

            plugin.getLogger().info("Monde de jeu '" + gameWorldName + "' chargé avec succès!");
        }
    }

    private void updateLevelDat(File worldDir) {
        // Cette méthode pourrait être améliorée pour modifier le level.dat
        // Pour l'instant, on laisse Bukkit gérer automatiquement
    }

    private void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyDirectory(srcFile, destFile);
                }
            }
        } else {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    private void teleportAllPlayersToMainWorld() {
        World mainWorld = Bukkit.getWorlds().get(0);

        for (Player player : plugin.getGameManager().getAllPlayers()) {
            player.teleport(mainWorld.getSpawnLocation());
        }
    }

    private void updateLocationsAfterReload() {
        // Mettre à jour les locations pour utiliser le nouveau monde
        if (gameWorld != null) {
            // Optionnel: Recharger les locations depuis la config
            // ou les mettre à jour programmatiquement
        }
    }

    /**
     * Sauvegarde le monde de jeu actuel comme template
     */
    public void saveCurrentWorldAsTemplate() {
        if (gameWorld == null) {
            plugin.getLogger().warning("Aucun monde de jeu chargé pour sauvegarder!");
            return;
        }

        try {
            // Forcer la sauvegarde du monde actuel
            gameWorld.save();

            // Créer un backup du template actuel
            if (plugin.getConfig().getBoolean("world.backup-before-reload", true)) {
                backupTemplateWorld();
            }

            // Copier le monde de jeu vers le template
            File gameWorldDir = new File(Bukkit.getWorldContainer(), gameWorldName);
            File templateDir = new File(Bukkit.getWorldContainer(), templateWorldName);

            deleteDirectory(templateDir);
            copyDirectory(gameWorldDir, templateDir);

            plugin.getLogger().info("Monde de jeu sauvegardé comme nouveau template!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde du template:", e);
        }
    }

    private void backupTemplateWorld() throws IOException {
        File templateDir = new File(Bukkit.getWorldContainer(), templateWorldName);
        File backupDir = new File(Bukkit.getWorldContainer(), templateWorldName + "_backup_" + System.currentTimeMillis());

        if (templateDir.exists()) {
            copyDirectory(templateDir, backupDir);
            plugin.getLogger().info("Backup du template créé: " + backupDir.getName());
        }
    }

    /**
     * Nettoie le monde de jeu (supprime les entités, items au sol, etc.)
     */
    public void cleanGameWorld() {
        if (gameWorld == null) return;

        // Supprimer tous les items au sol
        gameWorld.getEntities().forEach(entity -> {
            if (entity.getType().name().equals("DROPPED_ITEM") ||
                    entity.getType().name().equals("PRIMED_TNT") ||
                    entity.getType().name().equals("ARROW")) {
                entity.remove();
            }
        });

        plugin.getLogger().info("Monde de jeu nettoyé!");
    }

    // Getters
    public World getGameWorld() {
        return gameWorld;
    }

    public String getGameWorldName() {
        return gameWorldName;
    }

    public String getTemplateWorldName() {
        return templateWorldName;
    }

    public boolean isGameWorldLoaded() {
        return gameWorld != null;
    }
}