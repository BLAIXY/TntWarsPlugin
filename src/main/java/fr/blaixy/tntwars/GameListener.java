package fr.blaixy.tntwars;

import fr.blaixy.tntwars.GameManager;
import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;

public class GameListener implements Listener {

    private TNTWarsPlugin plugin;

    public GameListener(TNTWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameManager gameManager = plugin.getGameManager();

        // Messages de mort personnalisés
        if (gameManager.getGameState() == GameManager.GameState.PLAYING) {
            event.setDeathMessage("§c§l💀 " + player.getName() + " §c§la été pulvérisé par la TNT !");

            // Empêcher le drop d'items
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Permettre les explosions TNT seulement pendant le jeu
        if (plugin.getGameManager().getGameState() != GameManager.GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        // Limiter les dégâts aux blocs pour éviter de détruire complètement la map
        if (event.getEntity() instanceof TNTPrimed) {
            // Réduire la puissance de l'explosion
            event.setYield(0.5f);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Empêcher les dégâts entre joueurs sauf pendant le jeu
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (plugin.getGameManager().getGameState() != GameManager.GameState.PLAYING) {
                event.setCancelled(true);
            }
        }

        // Gérer les dégâts de TNT
        if (event.getEntity() instanceof Player && event.getDamager() instanceof TNTPrimed) {
            Player player = (Player) event.getEntity();

            if (plugin.getGameManager().getGameState() == GameManager.GameState.PLAYING) {
                // Augmenter les dégâts de TNT pour plus de fun
                event.setDamage(event.getDamage() * 2);

                player.sendMessage("§c§lAïe ! Vous avez pris des dégâts de TNT !");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // Empêcher de drop des items dans le lobby
        if (plugin.getGameManager().getGameState() != GameManager.GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();

        // Autoriser seulement pendant le jeu
        if (gameManager.getGameState() != GameManager.GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        // Empêcher de casser certains blocs importants
        Material blockType = event.getBlock().getType();
        if (blockType == Material.BEDROCK || blockType == Material.BARRIER) {
            event.setCancelled(true);
            player.sendMessage("§c§lVous ne pouvez pas casser ce bloc !");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();

        // Autoriser seulement pendant le jeu
        if (gameManager.getGameState() != GameManager.GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        // Empêcher de placer certains blocs
        Material blockType = event.getBlock().getType();
        if (blockType == Material.BEDROCK || blockType == Material.BARRIER) {
            event.setCancelled(true);
            player.sendMessage("§c§lVous ne pouvez pas placer ce bloc !");
        }

        // Limiter la hauteur de construction
        if (event.getBlock().getY() > 150) {
            event.setCancelled(true);
            player.sendMessage("§c§lVous ne pouvez pas construire si haut !");
        }
    }
}