package fr.blaixy.tntwars;

import fr.blaixy.tntwars.GameManager;
import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("tntwars", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§c§l⚡ TNT WARS ⚡");

        GameManager gameManager = TNTWarsPlugin.getInstance().getGameManager();
        GameManager.GameState state = gameManager.getGameState();

        int line = 15;

        // Ligne vide
        setScore(objective, "§r", line--);

        // État du jeu
        switch (state) {
            case WAITING:
                setScore(objective, "§7État: §eEn attente", line--);
                break;
            case STARTING:
                setScore(objective, "§7État: §aDémarrage", line--);
                setScore(objective, "§7Début: §c" + gameManager.getCountdown() + "s", line--);
                break;
            case PLAYING:
                setScore(objective, "§7État: §aEn cours", line--);
                break;
            case ENDING:
                setScore(objective, "§7État: §cFin", line--);
                break;
        }

        // Ligne vide
        setScore(objective, "§r ", line--);

        // Équipes
        setScore(objective, "§c§lÉquipe Rouge:", line--);
        int redCount = 0;
        for (Player p : gameManager.getRedTeam()) {
            if (p.isOnline()) {
                setScore(objective, "§c• " + p.getName(), line--);
                redCount++;
            }
        }
        if (redCount == 0) {
            setScore(objective, "§7• Aucun joueur", line--);
        }

        // Ligne vide
        setScore(objective, "§r  ", line--);

        setScore(objective, "§9§lÉquipe Bleue:", line--);
        int blueCount = 0;
        for (Player p : gameManager.getBlueTeam()) {
            if (p.isOnline()) {
                setScore(objective, "§9• " + p.getName(), line--);
                blueCount++;
            }
        }
        if (blueCount == 0) {
            setScore(objective, "§7• Aucun joueur", line--);
        }

        // Ligne vide
        setScore(objective, "§r   ", line--);

        // Informations
        setScore(objective, "§7Joueurs: §e" + gameManager.getAllPlayers().size() + "/4", line--);

        // Ligne vide
        setScore(objective, "§r    ", line--);

        // Site web ou IP
        setScore(objective, "§6play.tntwars.fr", line--);

        player.setScoreboard(board);
    }

    private void setScore(Objective objective, String text, int score) {
        Score s = objective.getScore(text);
        s.setScore(score);
    }
}