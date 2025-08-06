package dev.ua.ikeepcalm.happyNPCs.config;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class ConfigManager {

    private final HappyNPCs plugin;
    private FileConfiguration config;
    private File npcsFile;
    private FileConfiguration npcsConfig;
    private File progressFile;
    private FileConfiguration progressConfig;

    private boolean npcProtection;
    private String dialogueAdvanceKey;
    private String typingSound;
    private int typingSpeed;
    private String instructionBossbarUk;
    private String instructionBossbarEn;

    public ConfigManager(HappyNPCs plugin) {
        this.plugin = plugin;
        createFiles();
    }

    private void createFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        npcsFile = new File(plugin.getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            try {
                npcsFile.createNewFile();
                npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
                npcsConfig.createSection("npcs");
                npcsConfig.save(npcsFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create npcs.yml", e);
            }
        } else {
            npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
        }


        progressFile = new File(plugin.getDataFolder(), "progress.yml");
        if (!progressFile.exists()) {
            try {
                progressFile.createNewFile();
                progressConfig = YamlConfiguration.loadConfiguration(progressFile);
                progressConfig.createSection("progress");
                progressConfig.save(progressFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create progress.yml", e);
            }
        } else {
            progressConfig = YamlConfiguration.loadConfiguration(progressFile);
        }
    }

    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        npcProtection = config.getBoolean("npc-protection", true);
        dialogueAdvanceKey = config.getString("dialogue-advance-key", "F");
        typingSound = config.getString("typing-sound", "entity.experience_orb.pickup");
        typingSpeed = config.getInt("typing-speed", 2);
        instructionBossbarUk = config.getString("instruction-bossbar.uk", "<yellow>Натисни F щоб продовжити діалог</yellow>");
        instructionBossbarEn = config.getString("instruction-bossbar.en", "<yellow>Press F to continue dialogue</yellow>");

        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
        progressConfig = YamlConfiguration.loadConfiguration(progressFile);
    }

    public void saveNPCsConfig() {
        try {
            npcsConfig.save(npcsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save npcs.yml", e);
        }
    }

    public void saveProgressConfig() {
        try {
            progressConfig.save(progressFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save progress.yml", e);
        }
    }

    public int getPlayerDialogueProgress(String playerUUID, String dialogueId) {
        return progressConfig.getInt("progress." + playerUUID + "." + dialogueId, 1);
    }

    public void setPlayerDialogueProgress(String playerUUID, String dialogueId, int line) {
        progressConfig.set("progress." + playerUUID + "." + dialogueId, line);
    }

    public boolean hasCompletedDialogue(String playerUUID, String dialogueId) {
        return plugin.getDialogueManager().hasPlayerCompletedDialogue(playerUUID, dialogueId);
    }

    public String getInstructionBossbar(dev.ua.ikeepcalm.happyNPCs.locale.LocaleManager.SupportedLocale locale) {
        return locale == dev.ua.ikeepcalm.happyNPCs.locale.LocaleManager.SupportedLocale.UKRAINIAN 
            ? instructionBossbarUk 
            : instructionBossbarEn;
    }
}