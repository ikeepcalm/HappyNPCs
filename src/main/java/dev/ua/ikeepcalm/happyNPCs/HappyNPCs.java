package dev.ua.ikeepcalm.happyNPCs;

import dev.ua.ikeepcalm.happyNPCs.command.CommandManager;
import dev.ua.ikeepcalm.happyNPCs.config.ConfigManager;
import dev.ua.ikeepcalm.happyNPCs.listener.KeyPressListener;
import dev.ua.ikeepcalm.happyNPCs.listener.NPCInteractionListener;
import dev.ua.ikeepcalm.happyNPCs.listener.NPCMovementProtectionListener;
import dev.ua.ikeepcalm.happyNPCs.listener.NPCProtectionListener;
import dev.ua.ikeepcalm.happyNPCs.manager.DialogueManager;
import dev.ua.ikeepcalm.happyNPCs.manager.NPCManager;
import dev.ua.ikeepcalm.happyNPCs.util.FileWatcher;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

@Getter
public class HappyNPCs extends JavaPlugin {

    @Getter
    private static HappyNPCs instance;
    private ConfigManager configManager;
    private NPCManager npcManager;
    private DialogueManager dialogueManager;
    private CommandManager commandManager;
    private FileWatcher fileWatcher;
    private MiniMessage miniMessage;

    private boolean modelEngineAvailable;
    private boolean mythicMobsAvailable;

    @Override
    public void onEnable() {
        instance = this;
        miniMessage = MiniMessage.miniMessage();

        checkDependencies();
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        npcManager = new NPCManager(this);
        dialogueManager = new DialogueManager(this);
        commandManager = new CommandManager(this);

        getServer().getPluginManager().registerEvents(new NPCInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new KeyPressListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCMovementProtectionListener(this), this);

        commandManager.registerCommands();

        fileWatcher = new FileWatcher(this);
        fileWatcher.startWatching();

        loadData();

        new BukkitRunnable() {
            @Override
            public void run() {
                reloadAll();
            }
        }.runTaskLater(this, 20 * 1 * 60);

        getLogger().info("HappyNPCs has been enabled!");
    }

    @Override
    public void onDisable() {
        saveData();

        npcManager.removeAllNPCs();

        if (fileWatcher != null) {
            fileWatcher.stopWatching();
        }

        commandManager.unregisterCommands();

        getLogger().info("HappyNPCs has been disabled!");
    }

    public void loadData() {
        try {
            configManager.loadConfig();
            npcManager.loadNPCs();
            dialogueManager.loadDialogues();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load data", e);
        }
    }

    public void saveData() {
        try {
            npcManager.saveNPCs();
            dialogueManager.saveDialogueProgress();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to save data", e);
        }
    }

    public void reloadAll() {
        try {
            saveData();
            npcManager.despawnAllNPCs();
            configManager.loadConfig();
            npcManager.loadNPCs();
            dialogueManager.loadDialogues();
            getLogger().info("All configurations reloaded successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
        }
    }

    private void checkDependencies() {
        modelEngineAvailable = Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
        mythicMobsAvailable = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;

        if (modelEngineAvailable) {
            getLogger().info("ModelEngine found! ModelEngine integration enabled.");
        } else {
            getLogger().info("ModelEngine not found. ModelEngine features will be disabled.");
        }

        if (mythicMobsAvailable) {
            getLogger().info("MythicMobs found! MythicMobs integration enabled.");
        } else {
            getLogger().info("MythicMobs not found. MythicMobs features will be disabled.");
        }
    }

    public Component formatMessage(String message) {
        return miniMessage.deserialize(message);
    }

    public void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[HappyNPCs] ").color(NamedTextColor.AQUA)
                .append(formatMessage(message)));
    }

}