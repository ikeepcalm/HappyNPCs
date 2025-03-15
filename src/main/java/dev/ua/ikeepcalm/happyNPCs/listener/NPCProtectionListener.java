package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class NPCProtectionListener implements Listener {

    private final HappyNPCs plugin;
    
    public NPCProtectionListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected() && !event.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.CUSTOM)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getTarget();
        if (entity != null && plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (plugin.getNpcManager().isNPC(entity)) {
                HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
                if (npc != null) {
                    npc.despawn();
                }
            }
        }
    }
}