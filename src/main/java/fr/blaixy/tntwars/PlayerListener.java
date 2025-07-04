package fr.blaixy.tntwars;

import fr.blaixy.tntwars.GameManager;
import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private TNTWarsPlugin plugin;

    public PlayerListener(TNTWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Message de connexion personnalisé
        event.setJoinMessage("§a§l+ §a" + player.getName() + " §aa rejoint TNT Wars !");

        // Ajouter le joueur au jeu
        plugin.getGameManager().addPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Message de déconnexion personnalisé
        event.setQuitMessage("§c§l- §c" + player.getName() + " §ca quitté TNT Wars !");

        // Retirer le joueur du jeu
        plugin.getGameManager().removePlayer(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String displayName = item.getItemMeta().getDisplayName();

            // Boussole de sélection d'équipe
            if (displayName.equals("§e§lSélection d'équipe")) {
                plugin.getGameManager().openTeamSelectionMenu(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Vérifier si c'est le menu de sélection d'équipe
        if (event.getView().getTitle().equals("§c§lChoisissez votre équipe")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
                return;
            }

            String displayName = clickedItem.getItemMeta().getDisplayName();

            if (displayName.equals("§c§lÉquipe ROUGE")) {
                plugin.getGameManager().joinTeam(player, "rouge");
            } else if (displayName.equals("§9§lÉquipe BLEUE")) {
                plugin.getGameManager().joinTeam(player, "bleu");
            } else if (displayName.equals("§7§lSpectateur")) {
                // Optionnel: gérer le mode spectateur
                player.sendMessage("§7§lMode spectateur pas encore implémenté !");
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();

        // Si le joueur est dans une partie en cours
        if (gameManager.getGameState() == GameManager.GameState.PLAYING) {
            // Téléporter au lobby et le considérer comme éliminé
            event.setRespawnLocation(plugin.getLocationManager().getLobby());

            // Programmer la gestion de la mort après le respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                gameManager.handlePlayerDeath(player);
                player.sendMessage("§c§lVous avez été éliminé !");

                // Donner un item pour regarder
                player.getInventory().clear();
                ItemStack spectatorItem = new ItemStack(Material.COMPASS);
                spectatorItem.getItemMeta().setDisplayName("§e§lMode Spectateur");
                player.getInventory().setItem(4, spectatorItem);
            }, 1L);
        } else {
            // Respawn normal au lobby
            event.setRespawnLocation(plugin.getLocationManager().getLobby());
        }
    }
}