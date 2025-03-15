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
    private File dialoguesFile;
    private FileConfiguration dialoguesConfig;
    private File progressFile;
    private FileConfiguration progressConfig;

    private boolean npcProtection;
    private String dialogueAdvanceKey;
    private String typingSound;
    private int typingSpeed;

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

        dialoguesFile = new File(plugin.getDataFolder(), "dialogues.yml");
        if (!dialoguesFile.exists()) {
            try {
                if (plugin.getResource("dialogues.yml") != null) {
                    plugin.saveResource("dialogues.yml", false);
                } else {
                    dialoguesFile.createNewFile();
                    dialoguesConfig = YamlConfiguration.loadConfiguration(dialoguesFile);
                    dialoguesConfig.createSection("dialogues");
                    ConfigurationSection exampleSection = dialoguesConfig.createSection("dialogues.example");
                    exampleSection.set("lines", Map.of(
                            "1", "<gold>Hello there!</gold>",
                            "2", "<yellow>I'm an example NPC.</yellow>",
                            "3", "<green>Press F to continue the dialogue.</green>"
                    ));
                    exampleSection.set("actions", Map.of(
                            "3", "command:tell %player% Dialogue finished!"
                    ));
                    dialoguesConfig.save(dialoguesFile);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create dialogues.yml", e);
            }
        }
        dialoguesConfig = YamlConfiguration.loadConfiguration(dialoguesFile);

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

        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);

        File dialoguesBackup = new File(plugin.getDataFolder(), "dialogues.yml.backup");
        try {
            if (dialoguesFile.exists()) {
                Files.copy(dialoguesFile.toPath(), dialoguesBackup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            dialoguesConfig = YamlConfiguration.loadConfiguration(dialoguesFile);

            if (dialoguesBackup.exists()) {
                dialoguesBackup.delete();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading dialogues.yml. Reverting to backup.", e);
            if (dialoguesBackup.exists()) {
                try {
                    Files.copy(dialoguesBackup.toPath(), dialoguesFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    dialoguesConfig = YamlConfiguration.loadConfiguration(dialoguesFile);
                    plugin.getLogger().info("Successfully reverted to backup dialogues.yml");
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to revert to backup dialogues.yml", ex);
                }
            }
        }

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
        ConfigurationSection dialogue = dialoguesConfig.getConfigurationSection("dialogues." + dialogueId);
        if (dialogue == null) return false;

        int lastLine = 0;
        Set<String> lines = dialogue.getConfigurationSection("lines").getKeys(false);
        for (String lineKey : lines) {
            try {
                int lineNum = Integer.parseInt(lineKey);
                lastLine = Math.max(lastLine, lineNum);
            } catch (NumberFormatException ignored) {
            }
        }

        int progress = getPlayerDialogueProgress(playerUUID, dialogueId);
        return progress > lastLine;
    }
}