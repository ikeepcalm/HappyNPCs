package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NPCCleanupListener implements Listener {

    private final HappyNPCs plugin;
    
    public NPCCleanupListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : event.getWorld().getEntities()) {
                checkAndCleanupEntity(entity);
            }
        }, 40L);
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            for (Entity entity : event.getChunk().getEntities()) {
                checkAndCleanupEntity(entity);
            }
        }
    }
    
    private void checkAndCleanupEntity(Entity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (container.has(new NamespacedKey(plugin, "HappyNPC"))) {
            String npcId = container.get(new NamespacedKey(plugin, "HappyNPC"), PersistentDataType.STRING);
            HappyNPC npc = plugin.getNpcManager().getNPCMap().get(npcId);
            
            if (npc != null) {
                if (npc.getEntity() != null && !entity.getUniqueId().equals(npc.getEntity().getUniqueId())) {
                    plugin.getLogger().info("Removing duplicate NPC entity for " + npcId);
                    entity.remove();
                }
                else if (npc.getEntity() == null) {
                    npc.setEntityUUID(entity.getUniqueId());
                    plugin.getNpcManager().updateNPCEntity(null, entity.getUniqueId(), npc);
                    plugin.getLogger().info("Re-linked entity for NPC " + npcId);
                }
            } 
            else {
                plugin.getLogger().info("Removing orphaned NPC entity with ID: " + npcId);
                entity.remove();
            }
        }
    }
}