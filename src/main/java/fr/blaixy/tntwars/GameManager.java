package fr.blaixy.tntwars;

import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private TNTWarsPlugin plugin;
    private GameState gameState;
    private Set<Player> redTeam;
    private Set<Player> blueTeam;
    private Set<Player> allPlayers;
    private BukkitTask countdownTask;
    private BukkitTask gameTask;
    private int countdown;

    public enum GameState {
        WAITING,
        STARTING,
        PLAYING,
        ENDING
    }

    public GameManager(TNTWarsPlugin plugin) {
        this.plugin = plugin;
        this.gameState = GameState.WAITING;
        this.redTeam = new HashSet<>();
        this.blueTeam = new HashSet<>();
        this.allPlayers = new HashSet<>();
        this.countdown = 20;
    }

    public void addPlayer(Player player) {
        allPlayers.add(player);

        // Téléporter au lobby
        Location lobby = plugin.getLocationManager().getLobby();
        if (lobby != null) {
            player.teleport(lobby);
        }

        // Donner l'item de sélection d'équipe
        giveTeamSelector(player);

        // Mettre à jour le scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);

        // Vérifier si on peut commencer
        checkGameStart();

        // Messages de bienvenue
        player.sendMessage("§a§lBienvenue dans TNT Wars !");
        player.sendMessage("§e§lCliquez sur la laine pour choisir votre équipe !");
    }

    public void removePlayer(Player player) {
        allPlayers.remove(player);
        redTeam.remove(player);
        blueTeam.remove(player);

        // Vérifier si le jeu peut continuer
        if (gameState == GameState.PLAYING) {
            checkGameEnd();
        } else if (gameState == GameState.STARTING) {
            stopCountdown();
            gameState = GameState.WAITING;
            broadcastMessage("§c§lPas assez de joueurs ! Retour en attente...");
        }

        updateAllScoreboards();
    }

    public void joinTeam(Player player, String team) {
        // Retirer des équipes actuelles
        redTeam.remove(player);
        blueTeam.remove(player);

        if (team.equalsIgnoreCase("rouge") && redTeam.size() < 2) {
            redTeam.add(player);
            player.sendMessage("§c§lVous avez rejoint l'équipe ROUGE !");
            broadcastMessage("§a" + player.getName() + " §aa rejoint l'équipe §c§lROUGE§a !");
        } else if (team.equalsIgnoreCase("bleu") && blueTeam.size() < 2) {
            blueTeam.add(player);
            player.sendMessage("§9§lVous avez rejoint l'équipe BLEUE !");
            broadcastMessage("§a" + player.getName() + " §aa rejoint l'équipe §9§lBLEUE§a !");
        } else {
            player.sendMessage("§c§lÉquipe pleine ou invalide !");
            return;
        }

        updateAllScoreboards();
        checkGameStart();
    }

    private void giveTeamSelector(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);

        // Laine rouge
        ItemStack redWool = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta redMeta = redWool.getItemMeta();
        redMeta.setDisplayName("§c§lÉquipe ROUGE");
        redMeta.setLore(Arrays.asList("§7Cliquez pour rejoindre", "§7l'équipe rouge !"));
        redWool.setItemMeta(redMeta);

        // Laine bleue
        ItemStack blueWool = new ItemStack(Material.WOOL, 1, (short) 11);
        ItemMeta blueMeta = blueWool.getItemMeta();
        blueMeta.setDisplayName("§9§lÉquipe BLEUE");
        blueMeta.setLore(Arrays.asList("§7Cliquez pour rejoindre", "§7l'équipe bleue !"));
        blueWool.setItemMeta(blueMeta);

        player.getInventory().setItem(3, redWool);
        player.getInventory().setItem(5, blueWool);
    }

    private void checkGameStart() {
        if (gameState == GameState.WAITING && redTeam.size() == 2 && blueTeam.size() == 2) {
            startCountdown();
        }
    }

    private void startCountdown() {
        gameState = GameState.STARTING;
        countdown = 20;

        broadcastMessage("§a§lAssez de joueurs ! Début de la partie dans 20 secondes...");

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                if (countdown <= 10) {
                    broadcastTitle("§e§lDébut dans", "§c§l" + countdown + " seconde" + (countdown > 1 ? "s" : ""));
                    broadcastMessage("§e§lDébut dans §c§l" + countdown + " §e§lseconde" + (countdown > 1 ? "s" : "") + " !");
                }

                countdown--;
                updateAllScoreboards();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private void startGame() {
        gameState = GameState.PLAYING;

        // Téléporter les équipes
        Location redSpawn = plugin.getLocationManager().getRedSpawn();
        Location blueSpawn = plugin.getLocationManager().getBlueSpawn();

        for (Player player : redTeam) {
            if (redSpawn != null) {
                player.teleport(redSpawn);
            }
            player.setGameMode(GameMode.SURVIVAL);
            giveGameItems(player);
        }

        for (Player player : blueTeam) {
            if (blueSpawn != null) {
                player.teleport(blueSpawn);
            }
            player.setGameMode(GameMode.SURVIVAL);
            giveGameItems(player);
        }

        broadcastTitle("§a§lC'EST PARTI !", "§e§lBonne chance !");
        broadcastMessage("§a§l=== TNT WARS - C'EST PARTI ! ===");
        broadcastMessage("§e§lObjectif: Éliminez l'équipe adverse avec la TNT !");

        updateAllScoreboards();
    }

    private void giveGameItems(Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);

        // TNT
        player.getInventory().addItem(new ItemStack(Material.TNT, 16));

        // Briquet
        player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL));

        // Blocs pour construire
        player.getInventory().addItem(new ItemStack(Material.COBBLESTONE, 64));

        // Redstone
        player.getInventory().addItem(new ItemStack(Material.REDSTONE, 16));

        // Répéteurs
        player.getInventory().addItem(new ItemStack(Material.DIODE, 8));
    }

    public void handlePlayerDeath(Player player) {
        if (gameState != GameState.PLAYING) return;

        broadcastMessage("§c§l" + player.getName() + " §c§la été éliminé !");

        // Vérifier qui a gagné
        checkGameEnd();
    }

    private void checkGameEnd() {
        if (gameState != GameState.PLAYING) return;

        Set<Player> alivePlayers = new HashSet<>();
        String winnerTeam = null;

        for (Player player : redTeam) {
            if (player.isOnline() && !player.isDead()) {
                alivePlayers.add(player);
                if (winnerTeam == null) winnerTeam = "rouge";
            }
        }

        for (Player player : blueTeam) {
            if (player.isOnline() && !player.isDead()) {
                alivePlayers.add(player);
                if (winnerTeam == null) {
                    winnerTeam = "bleu";
                } else if (winnerTeam.equals("rouge")) {
                    winnerTeam = "none"; // Les deux équipes ont des survivants
                }
            }
        }

        // Vérifier si une équipe a gagné
        boolean redAlive = false;
        boolean blueAlive = false;

        for (Player player : redTeam) {
            if (player.isOnline() && !player.isDead()) {
                redAlive = true;
                break;
            }
        }

        for (Player player : blueTeam) {
            if (player.isOnline() && !player.isDead()) {
                blueAlive = true;
                break;
            }
        }

        if (!redAlive && blueAlive) {
            endGame("bleu");
        } else if (!blueAlive && redAlive) {
            endGame("rouge");
        } else if (!redAlive && !blueAlive) {
            endGame("égalité");
        }
    }

    private void endGame(String winner) {
        gameState = GameState.ENDING;

        if (winner.equals("rouge")) {
            broadcastTitle("§c§lÉQUIPE ROUGE GAGNE !", "§e§lFélicitations !");
            broadcastMessage("§c§l=== ÉQUIPE ROUGE GAGNE ! ===");
        } else if (winner.equals("bleu")) {
            broadcastTitle("§9§lÉQUIPE BLEUE GAGNE !", "§e§lFélicitations !");
            broadcastMessage("§9§l=== ÉQUIPE BLEUE GAGNE ! ===");
        } else {
            broadcastTitle("§7§lÉGALITÉ !", "§e§lBelle partie !");
            broadcastMessage("§7§l=== ÉGALITÉ ! ===");
        }

        // Programmer le redémarrage
        new BukkitRunnable() {
            @Override
            public void run() {
                restartGame();
            }
        }.runTaskLater(plugin, 100); // 5 secondes
    }

    public void endGame() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }

        gameState = GameState.ENDING;

        // Téléporter tous les joueurs au lobby
        Location lobby = plugin.getLocationManager().getLobby();
        for (Player player : allPlayers) {
            if (lobby != null) {
                player.teleport(lobby);
            }
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    private void restartGame() {
        gameState = GameState.WAITING;

        // Réinitialiser les équipes
        redTeam.clear();
        blueTeam.clear();

        // Téléporter au lobby et donner les items
        Location lobby = plugin.getLocationManager().getLobby();
        for (Player player : allPlayers) {
            if (lobby != null) {
                player.teleport(lobby);
            }
            player.setGameMode(GameMode.ADVENTURE);
            giveTeamSelector(player);
        }

        broadcastMessage("§a§lNouvelle partie ! Choisissez votre équipe !");
        updateAllScoreboards();
    }

    private void broadcastMessage(String message) {
        for (Player player : allPlayers) {
            player.sendMessage(message);
        }
    }

    private void broadcastTitle(String title, String subtitle) {
        for (Player player : allPlayers) {
            player.sendTitle(title, subtitle);
        }
    }

    private void updateAllScoreboards() {
        for (Player player : allPlayers) {
            plugin.getScoreboardManager().updateScoreboard(player);
        }
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public Set<Player> getRedTeam() {
        return redTeam;
    }

    public Set<Player> getBlueTeam() {
        return blueTeam;
    }

    public Set<Player> getAllPlayers() {
        return allPlayers;
    }

    public int getCountdown() {
        return countdown;
    }
}