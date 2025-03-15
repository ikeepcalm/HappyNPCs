package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class KeyPressListener implements Listener {

    private final HappyNPCs plugin;

    public KeyPressListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (plugin.getDialogueManager().isInDialogue(player)) {
            event.setCancelled(true);
            plugin.getDialogueManager().advanceDialogue(player);
        }
    }
}