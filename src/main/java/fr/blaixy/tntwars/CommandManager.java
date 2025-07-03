package fr.blaixy.tntwars;

import fr.blaixy.tntwars.GameManager;
import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

    private TNTWarsPlugin plugin;

    public CommandManager(TNTWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c§lSeuls les joueurs peuvent utiliser cette commande !");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "setlobby":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                plugin.getLocationManager().setLobby(player.getLocation());
                player.sendMessage("§a§lLobby défini à votre position !");
                break;

            case "setredspawn":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                plugin.getLocationManager().setRedSpawn(player.getLocation());
                player.sendMessage("§c§lSpawn de l'équipe rouge défini !");
                break;

            case "setbluespawn":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                plugin.getLocationManager().setBlueSpawn(player.getLocation());
                player.sendMessage("§9§lSpawn de l'équipe bleue défini !");
                break;

            case "forcestart":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                GameManager gameManager = plugin.getGameManager();
                if (gameManager.getGameState() == GameManager.GameState.WAITING) {
                    if (gameManager.getRedTeam().size() > 0 && gameManager.getBlueTeam().size() > 0) {
                        player.sendMessage("§a§lPartie forcée !");
                        gameManager.forceStart();
                    } else {
                        player.sendMessage("§c§lIl faut au moins 1 joueur par équipe !");
                    }
                } else {
                    player.sendMessage("§c§lLa partie n'est pas en attente !");
                }
                break;

            case "stop":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                plugin.getGameManager().endGame();
                player.sendMessage("§a§lPartie arrêtée !");
                break;

            case "resetmap":
                if (!player.hasPermission("tntwars.admin")) {
                    player.sendMessage("§c§lVous n'avez pas la permission !");
                    return true;
                }
                // Créer une instance du MapManager s'il n'existe pas
                if (plugin.getMapManager() == null) {
                    plugin.setMapManager(new MapManager(plugin));
                }
                plugin.getMapManager().resetMap();
                player.sendMessage("§a§lMap réinitialisée !");
                break;

            case "join":
                plugin.getGameManager().addPlayer(player);
                player.sendMessage("§a§lVous avez rejoint la partie !");
                break;

            case "leave":
                plugin.getGameManager().removePlayer(player);
                player.sendMessage("§c§lVous avez quitté la partie !");
                break;

            case "info":
                sendInfo(player);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§c§l=== TNT WARS - AIDE ===");
        player.sendMessage("§e/tntwars join §7- Rejoindre la partie");
        player.sendMessage("§e/tntwars leave §7- Quitter la partie");
        player.sendMessage("§e/tntwars info §7- Informations sur la partie");

        if (player.hasPermission("tntwars.admin")) {
            player.sendMessage("§c§l=== COMMANDES ADMIN ===");
            player.sendMessage("§e/tntwars setlobby §7- Définir le lobby");
            player.sendMessage("§e/tntwars setredspawn §7- Définir le spawn rouge");
            player.sendMessage("§e/tntwars setbluespawn §7- Définir le spawn bleu");
            player.sendMessage("§e/tntwars forcestart §7- Forcer le démarrage");
            player.sendMessage("§e/tntwars stop §7- Arrêter la partie");
            player.sendMessage("§e/tntwars resetmap §7- Réinitialiser la map");
        }
    }

    private void sendInfo(Player player) {
        GameManager gameManager = plugin.getGameManager();

        player.sendMessage("§c§l=== TNT WARS - INFORMATIONS ===");
        player.sendMessage("§7État: §e" + gameManager.getGameState());
        player.sendMessage("§7Joueurs: §e" + gameManager.getAllPlayers().size() + "/4");
        player.sendMessage("§c§lÉquipe Rouge: §e" + gameManager.getRedTeam().size() + "/2");
        for (Player p : gameManager.getRedTeam()) {
            player.sendMessage("§c• " + p.getName());
        }
        player.sendMessage("§9§lÉquipe Bleue: §e" + gameManager.getBlueTeam().size() + "/2");
        for (Player p : gameManager.getBlueTeam()) {
            player.sendMessage("§9• " + p.getName());
        }

        if (gameManager.getGameState() == GameManager.GameState.STARTING) {
            player.sendMessage("§7Démarrage dans: §c" + gameManager.getCountdown() + " secondes");
        }
    }
}