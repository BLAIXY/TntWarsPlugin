package fr.blaixy.tntwars;

import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NPCManager {

    private TNTWarsPlugin plugin;
    private Map<String, Villager> npcs;
    private Map<String, Location> npcLocations;

    public NPCManager(TNTWarsPlugin plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();
        this.npcLocations = new HashMap<>();
        loadNPCLocations();
    }

    private void loadNPCLocations() {
        // Charger les locations des NPCs depuis la config
        plugin.getConfig().addDefault("npcs.red-supplier.location", "");
        plugin.getConfig().addDefault("npcs.blue-supplier.location", "");
        plugin.getConfig().addDefault("npcs.red-supplier.name", "§c§lFournisseur Rouge");
        plugin.getConfig().addDefault("npcs.blue-supplier.name", "§9§lFournisseur Bleu");
        plugin.saveConfig();
    }

    /**
     * Spawn les NPCs aux locations définies
     */
    public void spawnNPCs() {
        // Supprimer les anciens NPCs
        removeAllNPCs();

        // Spawn NPC équipe rouge
        Location redLocation = getLocationFromConfig("npcs.red-supplier.location");
        if (redLocation != null) {
            spawnSupplierNPC("red", redLocation);
        }

        // Spawn NPC équipe bleue
        Location blueLocation = getLocationFromConfig("npcs.blue-supplier.location");
        if (blueLocation != null) {
            spawnSupplierNPC("blue", blueLocation);
        }
    }

    private void spawnSupplierNPC(String team, Location location) {
        Villager npc = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        // Configuration du NPC
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setCollidable(false);
        npc.setRemoveWhenFarAway(false);
        npc.setProfession(Villager.Profession.TOOLSMITH);

        // Nom du NPC
        String npcName = team.equals("red") ?
                plugin.getConfig().getString("npcs.red-supplier.name", "§c§lFournisseur Rouge") :
                plugin.getConfig().getString("npcs.blue-supplier.name", "§9§lFournisseur Bleu");

        npc.setCustomName(npcName);
        npc.setCustomNameVisible(true);

        // Stocker le NPC
        npcs.put(team + "_supplier", npc);

        plugin.getLogger().info("NPC " + npcName + " spawné à " + location);
    }

    /**
     * Supprime tous les NPCs
     */
    public void removeAllNPCs() {
        for (Villager npc : npcs.values()) {
            if (npc != null && !npc.isDead()) {
                npc.remove();
            }
        }
        npcs.clear();
    }

    /**
     * Définit la location d'un NPC
     */
    public void setNPCLocation(String team, Location location) {
        String configKey = "npcs." + team + "-supplier.location";
        plugin.getConfig().set(configKey, locationToString(location));
        plugin.saveConfig();

        // Respawn le NPC à la nouvelle location
        spawnSupplierNPC(team, location);
    }

    /**
     * Ouvre le menu de loot pour un joueur
     */
    public void openLootMenu(Player player, String team) {
        // Vérifier si le joueur appartient à la bonne équipe
        if (!isPlayerInTeam(player, team)) {
            player.sendMessage("§c§lVous ne pouvez utiliser que le fournisseur de votre équipe !");
            return;
        }

        // Vérifier si le jeu est en cours
        if (plugin.getGameManager().getGameState() != GameManager.GameState.PLAYING) {
            player.sendMessage("§c§lLe fournisseur n'est disponible qu'en jeu !");
            return;
        }

        Inventory menu = createLootMenu(team);
        player.openInventory(menu);
    }

    private Inventory createLootMenu(String team) {
        String title = team.equals("red") ? "§c§lFournisseur Rouge" : "§9§lFournisseur Bleu";
        Inventory menu = Bukkit.createInventory(null, 54, title);

        // TNT
        ItemStack tnt = new ItemStack(Material.TNT, 8);
        ItemMeta tntMeta = tnt.getItemMeta();
        tntMeta.setDisplayName("§c§lTNT");
        tntMeta.setLore(Arrays.asList("§7Explosive principale", "§7Quantité: 8"));
        tnt.setItemMeta(tntMeta);

        // Briquet
        ItemStack flint = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta flintMeta = flint.getItemMeta();
        flintMeta.setDisplayName("§6§lBriquet");
        flintMeta.setLore(Arrays.asList("§7Pour allumer la TNT"));
        flint.setItemMeta(flintMeta);

        // Blocs de construction
        ItemStack cobble = new ItemStack(Material.COBBLESTONE, 32);
        ItemMeta cobbleMeta = cobble.getItemMeta();
        cobbleMeta.setDisplayName("§7§lPierre");
        cobbleMeta.setLore(Arrays.asList("§7Blocs de construction", "§7Quantité: 32"));
        cobble.setItemMeta(cobbleMeta);

        // Redstone
        ItemStack redstone = new ItemStack(Material.REDSTONE, 16);
        ItemMeta redstoneMeta = redstone.getItemMeta();
        redstoneMeta.setDisplayName("§4§lRedstone");
        redstoneMeta.setLore(Arrays.asList("§7Pour les circuits", "§7Quantité: 16"));
        redstone.setItemMeta(redstoneMeta);

        // Répéteurs
        ItemStack repeater = new ItemStack(Material.DIODE, 4);
        ItemMeta repeaterMeta = repeater.getItemMeta();
        repeaterMeta.setDisplayName("§e§lRépéteur");
        repeaterMeta.setLore(Arrays.asList("§7Circuit redstone", "§7Quantité: 4"));
        repeater.setItemMeta(repeaterMeta);

        // Plaque de pression
        ItemStack pressurePlate = new ItemStack(Material.STONE_PLATE, 2);
        ItemMeta plateMeta = pressurePlate.getItemMeta();
        plateMeta.setDisplayName("§8§lPlaque de pression");
        plateMeta.setLore(Arrays.asList("§7Déclencheur", "§7Quantité: 2"));
        pressurePlate.setItemMeta(plateMeta);

        // Bouton
        ItemStack button = new ItemStack(Material.STONE_BUTTON, 2);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName("§8§lBouton");
        buttonMeta.setLore(Arrays.asList("§7Déclencheur", "§7Quantité: 2"));
        button.setItemMeta(buttonMeta);

        // Leviers
        ItemStack lever = new ItemStack(Material.LEVER, 2);
        ItemMeta leverMeta = lever.getItemMeta();
        leverMeta.setDisplayName("§6§lLevier");
        leverMeta.setLore(Arrays.asList("§7Interrupteur", "§7Quantité: 2"));
        lever.setItemMeta(leverMeta);

        // Nourriture
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 8);
        ItemMeta foodMeta = food.getItemMeta();
        foodMeta.setDisplayName("§d§lNourriture");
        foodMeta.setLore(Arrays.asList("§7Récupère la faim", "§7Quantité: 8"));
        food.setItemMeta(foodMeta);

        // Potion de soins
        ItemStack healPotion = new ItemStack(Material.POTION, 2);
        ItemMeta healMeta = healPotion.getItemMeta();
        healMeta.setDisplayName("§c§lPotion de soins");
        healMeta.setLore(Arrays.asList("§7Récupère la vie", "§7Quantité: 2"));
        healPotion.setItemMeta(healMeta);

        // Placement des items dans le menu
        menu.setItem(10, tnt);
        menu.setItem(11, flint);
        menu.setItem(12, cobble);
        menu.setItem(13, redstone);
        menu.setItem(14, repeater);
        menu.setItem(15, pressurePlate);
        menu.setItem(16, button);
        menu.setItem(19, lever);
        menu.setItem(20, food);
        menu.setItem(21, healPotion);

        // Items décoratifs
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) (team.equals("red") ? 14 : 11));
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName("§r");
        glass.setItemMeta(glassMeta);

        // Remplir les slots vides avec du verre coloré
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, glass);
            }
        }

        // Bouton de fermeture
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§lFermer");
        close.setItemMeta(closeMeta);
        menu.setItem(49, close);

        return menu;
    }

    /**
     * Gère les clics dans le menu de loot
     */
    public void handleLootMenuClick(Player player, ItemStack clickedItem, String team) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Vérifier si c'est le bouton de fermeture
        if (itemName.equals("§c§lFermer")) {
            player.closeInventory();
            return;
        }

        // Vérifier si l'inventaire du joueur a de la place
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§c§lVotre inventaire est plein !");
            return;
        }

        // Donner l'item au joueur
        ItemStack itemToGive = clickedItem.clone();

        // Supprimer les métadonnées de lore pour l'item donné
        ItemMeta meta = itemToGive.getItemMeta();
        meta.setLore(null);
        itemToGive.setItemMeta(meta);

        player.getInventory().addItem(itemToGive);
        player.sendMessage("§a§lVous avez reçu: " + itemName);

        // Effet sonore
        player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.0f);
    }

    /**
     * Vérifie si le joueur appartient à l'équipe spécifiée
     */
    private boolean isPlayerInTeam(Player player, String team) {
        GameManager gameManager = plugin.getGameManager();

        if (team.equals("red")) {
            return gameManager.getRedTeam().contains(player);
        } else if (team.equals("blue")) {
            return gameManager.getBlueTeam().contains(player);
        }

        return false;
    }

    /**
     * Obtient la location depuis la config
     */
    private Location getLocationFromConfig(String path) {
        String locationString = plugin.getConfig().getString(path);
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        return stringToLocation(locationString);
    }

    /**
     * Convertit une chaîne en Location
     */
    private Location stringToLocation(String locationString) {
        try {
            String[] parts = locationString.split(",");
            if (parts.length != 6) return null;

            org.bukkit.World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convertit une Location en chaîne
     */
    private String locationToString(Location location) {
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * Vérifie si un joueur clique sur un NPC
     */
    public boolean isNPCClick(org.bukkit.entity.Entity entity, Player player) {
        if (!(entity instanceof Villager)) return false;

        Villager villager = (Villager) entity;

        // Vérifier si c'est un de nos NPCs
        for (Map.Entry<String, Villager> entry : npcs.entrySet()) {
            if (entry.getValue().equals(villager)) {
                String[] parts = entry.getKey().split("_");
                if (parts.length > 0) {
                    String team = parts[0];
                    openLootMenu(player, team);
                    return true;
                }
            }
        }

        return false;
    }

    // Getters
    public Map<String, Villager> getNPCs() {
        return npcs;
    }
}