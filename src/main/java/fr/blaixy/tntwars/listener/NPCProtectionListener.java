package fr.blaixy.tntwars.listener;

import fr.blaixy.tntwars.NPCManager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class NPCProtectionListener implements Listener {

    private final NPCManager npcManager;

    public NPCProtectionListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            if (npcManager.getNPCs().containsValue(villager)) {
                event.setCancelled(true);
            }
        }
    }
}