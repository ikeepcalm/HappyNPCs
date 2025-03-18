package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class NPCMovementProtectionListener implements Listener {

    private final HappyNPCs plugin;
    
    public NPCMovementProtectionListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFish(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught != null && plugin.getNpcManager().isNPC(caught)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(caught);
            if (npc != null && npc.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityKnockback(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (plugin.getNpcManager().isNPC(entity)) {
            HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
            if (npc != null && npc.isProtected()) {
                entity.setVelocity(entity.getVelocity().zero());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Entity entity : event.getEntity().getNearbyEntities(5, 5, 5)) {
            if (plugin.getNpcManager().isNPC(entity)) {
                HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
                if (npc != null && npc.isProtected()) {
                    entity.setVelocity(entity.getVelocity().zero());
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Entity entity : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation(), 5, 5, 5)) {
            if (plugin.getNpcManager().isNPC(entity)) {
                HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
                if (npc != null && npc.isProtected()) {
                    entity.setVelocity(entity.getVelocity().zero());
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        checkPistonMovement(event.getBlocks(), event);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        checkPistonMovement(event.getBlocks(), event);
    }
    
    private void checkPistonMovement(List<org.bukkit.block.Block> blocks, org.bukkit.event.Cancellable event) {
        for (org.bukkit.block.Block block : blocks) {
            for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1)) {
                if (plugin.getNpcManager().isNPC(entity)) {
                    HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
                    if (npc != null && npc.isProtected()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}