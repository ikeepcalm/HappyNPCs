package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLocaleListener implements Listener {

    private final HappyNPCs plugin;

    public PlayerLocaleListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLocaleManager().getPlayerLocale(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLocaleManager().removePlayer(event.getPlayer().getUniqueId());
    }
}