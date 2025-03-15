package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.manager.DialogueManager;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCInteractionListener implements Listener {

    private final HappyNPCs plugin;
    private final Map<UUID, Long> interactionCooldowns = new HashMap<>();
    private static final long INTERACTION_COOLDOWN = 2000;
    
    public NPCInteractionListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        handleInteraction(event.getPlayer(), event.getRightClicked());
    }
    
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        handleInteraction(event.getPlayer(), event.getRightClicked());
    }
    
    private void handleInteraction(Player player, Entity entity) {
        if (!plugin.getNpcManager().isNPC(entity)) {
            return;
        }

        HappyNPC npc = plugin.getNpcManager().getNPCByEntity(entity);
        if (npc == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastInteraction = interactionCooldowns.get(player.getUniqueId());
        if (lastInteraction != null && now - lastInteraction < INTERACTION_COOLDOWN) {
            return;
        }
        interactionCooldowns.put(player.getUniqueId(), now);

        DialogueManager dialogueManager = plugin.getDialogueManager();
        if (dialogueManager.isInDialogue(player)) {
            return;
        }
        
        String dialogueId = npc.getDialogueId();
        if (dialogueId != null && !dialogueId.isEmpty()) {
            dialogueManager.startDialogue(player, npc);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getNpcManager().showNPCsTo(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        DialogueManager dialogueManager = plugin.getDialogueManager();
        if (dialogueManager.isInDialogue(event.getPlayer())) {
            dialogueManager.endDialogue(event.getPlayer());
        }


        interactionCooldowns.remove(event.getPlayer().getUniqueId());
    }
}