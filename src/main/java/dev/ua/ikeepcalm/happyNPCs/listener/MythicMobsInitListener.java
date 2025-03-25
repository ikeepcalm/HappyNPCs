package dev.ua.ikeepcalm.happyNPCs.listener;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MythicMobsInitListener implements Listener {
    
    private final HappyNPCs plugin;
    private boolean mythicMobsInitialized = false;
    
    public MythicMobsInitListener(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("MythicMobs") && !mythicMobsInitialized) {
            plugin.getLogger().info("MythicMobs has been enabled, initializing NPCs with delay...");
            mythicMobsInitialized = true;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getLogger().info("Respawning all NPCs with MythicMobs...");
                    for (var npc : plugin.getNpcManager().getAllNPCs()) {
                        if (!npc.getMythicMobId().isEmpty()) {
                            npc.despawn();
                            npc.spawn();
                        }
                    }
                }
            }.runTaskLater(plugin, 60L);
        }
    }
}