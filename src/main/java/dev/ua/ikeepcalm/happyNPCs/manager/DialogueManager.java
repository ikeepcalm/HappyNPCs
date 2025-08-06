package dev.ua.ikeepcalm.happyNPCs.manager;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.locale.LocaleManager;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Getter
@Setter
public class DialogueManager {

    private final HappyNPCs plugin;
    private final Map<String, Map<Integer, LocalizedLine>> dialogues = new HashMap<>();
    private final Map<String, Map<Integer, String>> actions = new HashMap<>();
    private final Map<String, Integer> restartLines = new HashMap<>();
    private final Map<UUID, ActiveDialogue> activeDialogues = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> typingTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> instructionBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastInteractionTime = new ConcurrentHashMap<>();
    private final File dialoguesFolder;

    public DialogueManager(HappyNPCs plugin) {
        this.plugin = plugin;
        this.dialoguesFolder = new File(plugin.getDataFolder(), "dialogues");

        if (!dialoguesFolder.exists()) {
            dialoguesFolder.mkdirs();
        }
    }

    public void loadDialogues() {
        dialogues.clear();
        actions.clear();
        restartLines.clear();
        loadIndividualDialogues();
        plugin.getLogger().info("Loaded " + dialogues.size() + " dialogues");
    }

    private void loadIndividualDialogues() {
        if (!dialoguesFolder.exists() || !dialoguesFolder.isDirectory()) {
            plugin.getLogger().warning("Dialogues folder does not exist or is not a directory");
            return;
        }

        File[] files = dialoguesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No dialogue files found in " + dialoguesFolder.getPath());
            createExampleDialogueFile();
            return;
        }

        for (File file : files) {
            String dialogueId = file.getName().substring(0, file.getName().length() - 4); // Remove .yml

            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadDialogueFromFile(dialogueId, config);
                plugin.getLogger().info("Loaded dialogue " + dialogueId + " from " + file.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load dialogue from " + file.getName(), e);

                File backupFile = new File(dialoguesFolder, file.getName() + ".backup");
                if (backupFile.exists()) {
                    try {
                        FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);
                        loadDialogueFromFile(dialogueId, backupConfig);
                        plugin.getLogger().info("Loaded backup dialogue for " + dialogueId);
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to load backup dialogue for " + dialogueId, ex);
                    }
                }
            }
        }
    }

    private void loadDialogueFromFile(String dialogueId, FileConfiguration config) {
        Map<Integer, LocalizedLine> lines = new HashMap<>();
        Map<Integer, String> dialogueActions = new HashMap<>();

        int restartLine = config.getInt("restart-line", 1);
        restartLines.put(dialogueId, restartLine);

        ConfigurationSection linesSection = config.getConfigurationSection("lines");
        if (linesSection == null) {
            plugin.getLogger().warning("No lines section found in dialogue " + dialogueId);
            return;
        }

        for (String lineKey : linesSection.getKeys(false)) {
            try {
                int lineNum = Integer.parseInt(lineKey);
                ConfigurationSection lineSection = linesSection.getConfigurationSection(lineKey);
                
                if (lineSection != null) {
                    String ukText = lineSection.getString("uk");
                    String enText = lineSection.getString("en");
                    
                    if (ukText != null && enText != null) {
                        lines.put(lineNum, new LocalizedLine(ukText, enText));
                    } else {
                        plugin.getLogger().warning("Missing uk or en text for line " + lineKey + " in dialogue " + dialogueId);
                    }
                } else {
                    String lineText = linesSection.getString(lineKey);
                    if (lineText != null) {
                        lines.put(lineNum, new LocalizedLine(lineText, lineText));
                    }
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid line number '" + lineKey + "' in dialogue " + dialogueId);
            }
        }

        ConfigurationSection actionsSection = config.getConfigurationSection("actions");
        if (actionsSection != null) {
            for (String actionKey : actionsSection.getKeys(false)) {
                try {
                    int lineNum = Integer.parseInt(actionKey);
                    String actionText = actionsSection.getString(actionKey);
                    dialogueActions.put(lineNum, actionText);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid action line number '" + actionKey + "' in dialogue " + dialogueId);
                }
            }
        }

        dialogues.put(dialogueId, lines);
        actions.put(dialogueId, dialogueActions);
    }

    private void createExampleDialogueFile() {
        File exampleFile = new File(dialoguesFolder, "example.yml");
        if (exampleFile.exists()) {
            return;
        }

        try {
            FileConfiguration config = new YamlConfiguration();

            config.set("restart-line", 1);

            ConfigurationSection linesSection = config.createSection("lines");
            
            ConfigurationSection line1Section = linesSection.createSection("1");
            line1Section.set("uk", "<gold>Привіт!</gold>");
            line1Section.set("en", "<gold>Hello there!</gold>");
            
            ConfigurationSection line2Section = linesSection.createSection("2");
            line2Section.set("uk", "<yellow>Я приклад НПС.</yellow>");
            line2Section.set("en", "<yellow>I'm an example NPC.</yellow>");
            
            ConfigurationSection line3Section = linesSection.createSection("3");
            line3Section.set("uk", "<green>Натисни F щоб продовжити діалог.</green>");
            line3Section.set("en", "<green>Press F to continue the dialogue.</green>");

            ConfigurationSection actionsSection = config.createSection("actions");
            actionsSection.set("3", "command:tell %player% Dialogue finished!");

            config.save(exampleFile);
            plugin.getLogger().info("Created example dialogue file");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create example dialogue file", e);
        }
    }

    public void saveDialogueProgress() {
        plugin.getConfigManager().saveProgressConfig();
    }

    public void startDialogue(Player player, HappyNPC npc) {
        String dialogueId = npc.getDialogueId();
        if (dialogueId == null || dialogueId.isEmpty()) {
            return;
        }

        Map<Integer, LocalizedLine> lines = dialogues.get(dialogueId);
        if (lines == null || lines.isEmpty()) {
            plugin.getLogger().warning("Dialogue " + dialogueId + " not found or empty for NPC " + npc.getId());
            return;
        }

        if (activeDialogues.containsKey(player.getUniqueId())) {
            return;
        }

        int startLine = 1;
        boolean completed = plugin.getConfigManager().hasCompletedDialogue(player.getUniqueId().toString(), dialogueId);
        if (completed) {
            startLine = restartLines.getOrDefault(dialogueId, 1);
        } else {
            startLine = plugin.getConfigManager().getPlayerDialogueProgress(player.getUniqueId().toString(), dialogueId);
        }

        ActiveDialogue activeDialogue = new ActiveDialogue(dialogueId, npc, startLine);
        activeDialogues.put(player.getUniqueId(), activeDialogue);

        // Show instruction bossbar if it's been more than 30 seconds since last interaction
        long currentTime = System.currentTimeMillis();
        long lastInteraction = lastInteractionTime.getOrDefault(player.getUniqueId(), 0L);
        if (currentTime - lastInteraction > 30000) { // 30 seconds
            showInstructionBossBar(player);
        }
        lastInteractionTime.put(player.getUniqueId(), currentTime);

        showDialogueLine(player, activeDialogue);

    }

    public void advanceDialogue(Player player) {
        ActiveDialogue activeDialogue = activeDialogues.get(player.getUniqueId());
        if (activeDialogue == null) {
            return;
        }

        BukkitTask typingTask = typingTasks.remove(player.getUniqueId());
        if (typingTask != null) {
            typingTask.cancel();
        }

        BossBar instructionBar = instructionBossBars.remove(player.getUniqueId());
        if (instructionBar != null) {
            player.hideBossBar(instructionBar);
        }

        if (!activeDialogue.isLineCompleted()) {
            String dialogueId = activeDialogue.getDialogueId();
            Map<Integer, LocalizedLine> lines = dialogues.get(dialogueId);
            LocalizedLine localizedLine = lines.get(activeDialogue.getCurrentLine());

            if (localizedLine != null) {
                LocaleManager.SupportedLocale playerLocale = plugin.getLocaleManager().getPlayerLocale(player);
                String fullLine = localizedLine.getText(playerLocale);
                Component message = plugin.getMiniMessage().deserialize(fullLine);
                player.sendActionBar(message);
                activeDialogue.setLineCompleted(true);
                return;
            }
        }

        executeLineAction(player, activeDialogue);

        String dialogueId = activeDialogue.getDialogueId();
        Map<Integer, LocalizedLine> lines = dialogues.get(dialogueId);

        if (!lines.containsKey(activeDialogue.getCurrentLine() + 1)) {
            activeDialogues.remove(player.getUniqueId());
            plugin.getConfigManager().setPlayerDialogueProgress(player.getUniqueId().toString(), dialogueId, restartLines.getOrDefault(dialogueId, 1));
            saveDialogueProgress();
            return;
        }

        activeDialogue.setCurrentLine(activeDialogue.getCurrentLine() + 1);

        plugin.getConfigManager().setPlayerDialogueProgress(
                player.getUniqueId().toString(),
                activeDialogue.getDialogueId(),
                activeDialogue.getCurrentLine()
        );

        showDialogueLine(player, activeDialogue);
    }

    private void showDialogueLine(Player player, ActiveDialogue activeDialogue) {
        String dialogueId = activeDialogue.getDialogueId();
        int currentLine = activeDialogue.getCurrentLine();

        Map<Integer, LocalizedLine> lines = dialogues.get(dialogueId);
        LocalizedLine localizedLine = lines.get(currentLine);

        if (localizedLine == null) {
            plugin.getLogger().warning("Line " + currentLine + " not found in dialogue " + dialogueId);
            activeDialogues.remove(player.getUniqueId());
            plugin.getConfigManager().setPlayerDialogueProgress(player.getUniqueId().toString(), dialogueId, 1);
            saveDialogueProgress();
            return;
        }

        LocaleManager.SupportedLocale playerLocale = plugin.getLocaleManager().getPlayerLocale(player);
        String lineText = localizedLine.getText(playerLocale);

        activeDialogue.setLineCompleted(false);

        startTypingEffect(player, lineText);
    }

    private void startTypingEffect(Player player, String fullText) {
        int typingSpeed = plugin.getConfigManager().getTypingSpeed();
        String typingSoundName = plugin.getConfigManager().getTypingSound();

        final StringBuilder currentText = new StringBuilder();
        final int[] charIndex = {0};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (charIndex[0] >= fullText.length()) {
                BukkitTask typingTask = typingTasks.remove(player.getUniqueId());
                if (typingTask != null) {
                    typingTask.cancel();
                }

                ActiveDialogue activeDialogue = activeDialogues.get(player.getUniqueId());
                if (activeDialogue != null) {
                    activeDialogue.setLineCompleted(true);
                }
                return;
            }

            currentText.append(fullText.charAt(charIndex[0]));
            charIndex[0]++;

            String visibleText = extractVisibleText(fullText, charIndex[0]);
            Component message = plugin.getMiniMessage().deserialize(fullText);
            Component partialMessage = Component.text(visibleText).style(message.style());
            player.sendActionBar(partialMessage);

            if (charIndex[0] % 4 == 0) {
                Sound sound = Sound.sound(
                        Key.key(typingSoundName),
                        Sound.Source.PLAYER,
                        0.5f,
                        1.0f + (float) (Math.random() * 0.5 - 0.20)
                );
                player.playSound(sound);
            }
        }, 0, typingSpeed);

        typingTasks.put(player.getUniqueId(), task);
    }

    private String extractVisibleText(String fullText, int charIndex) {
        StringBuilder visibleText = new StringBuilder();
        boolean inTag = false;
        int visibleCharCount = 0;

        for (int i = 0; i < fullText.length() && visibleCharCount < charIndex; i++) {
            char c = fullText.charAt(i);

            if (c == '<') {
                inTag = true;
                continue;
            }

            if (inTag) {
                if (c == '>') {
                    inTag = false;
                }
                continue;
            }

            visibleText.append(c);
            visibleCharCount++;
        }

        return visibleText.toString();
    }

    private void executeLineAction(Player player, ActiveDialogue activeDialogue) {
        String dialogueId = activeDialogue.getDialogueId();
        int currentLine = activeDialogue.getCurrentLine();

        Map<Integer, String> dialogueActions = actions.get(dialogueId);
        if (dialogueActions == null || !dialogueActions.containsKey(currentLine)) {
            return;
        }

        String actionString = dialogueActions.get(currentLine);
        if (actionString == null || actionString.isEmpty()) {
            return;
        }

        String[] actionParts = actionString.split(":", 2);
        if (actionParts.length < 2) {
            plugin.getLogger().warning("Invalid action format: " + actionString);
            return;
        }

        String actionType = actionParts[0].toLowerCase();
        String actionData = actionParts[1];

        actionData = actionData.replace("%player%", player.getName());

        switch (actionType) {
            case "command":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), actionData);
                break;
            case "player_command":
                player.performCommand(actionData);
                break;
            case "sound":
                try {
                    String[] soundParts = actionData.split(",");
                    String soundName = soundParts[0];
                    float volume = soundParts.length > 1 ? Float.parseFloat(soundParts[1]) : 1.0f;
                    float pitch = soundParts.length > 2 ? Float.parseFloat(soundParts[2]) : 1.0f;

                    Sound sound = Sound.sound(
                            Key.key(soundName),
                            Sound.Source.PLAYER,
                            volume,
                            pitch
                    );
                    player.playSound(sound);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid sound action: " + actionData, e);
                }
                break;
            case "title":
                try {
                    String[] titleParts = actionData.split(",", 3);
                    String titleText = titleParts[0];
                    String subtitleText = titleParts.length > 1 ? titleParts[1] : "";
                    int duration = titleParts.length > 2 ? Integer.parseInt(titleParts[2]) : 70;

                    Component title = plugin.getMiniMessage().deserialize(titleText);
                    Component subtitle = plugin.getMiniMessage().deserialize(subtitleText);

                    Title.Times times = Title.Times.of(
                            Duration.ofMillis(500),
                            Duration.ofMillis(duration * 50),
                            Duration.ofMillis(500)
                    );

                    player.showTitle(Title.title(title, subtitle, times));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid title action: " + actionData, e);
                }
                break;
            case "message":
                Component message = plugin.getMiniMessage().deserialize(actionData);
                player.sendMessage(message);
                break;
            default:
                plugin.getLogger().warning("Unknown action type: " + actionType);
                break;
        }
    }

    public boolean isInDialogue(Player player) {
        return activeDialogues.containsKey(player.getUniqueId());
    }

    public void endDialogue(Player player) {
        activeDialogues.remove(player.getUniqueId());
        BukkitTask typingTask = typingTasks.remove(player.getUniqueId());
        if (typingTask != null) {
            typingTask.cancel();
        }
        
        // Remove instruction bossbar when ending dialogue
        BossBar instructionBar = instructionBossBars.remove(player.getUniqueId());
        if (instructionBar != null) {
            player.hideBossBar(instructionBar);
        }
        
        player.sendActionBar(Component.empty());
    }

    private void showInstructionBossBar(Player player) {
        LocaleManager.SupportedLocale playerLocale = plugin.getLocaleManager().getPlayerLocale(player);
        String instructionText = plugin.getConfigManager().getInstructionBossbar(playerLocale);
        Component instructionComponent = plugin.getMiniMessage().deserialize(instructionText);
        
        BossBar bossBar = BossBar.bossBar(
            instructionComponent,
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
        );
        
        instructionBossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BossBar currentBar = instructionBossBars.remove(player.getUniqueId());
            if (currentBar != null && currentBar.equals(bossBar)) {
                player.hideBossBar(bossBar);
            }
        }, 100L); // 5 seconds
    }

    public boolean hasPlayerCompletedDialogue(String playerUUID, String dialogueId) {
        Map<Integer, LocalizedLine> lines = dialogues.get(dialogueId);
        if (lines == null || lines.isEmpty()) {
            return false;
        }

        int lastLine = lines.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        
        int progress = plugin.getConfigManager().getPlayerDialogueProgress(playerUUID, dialogueId);
        
        return progress > lastLine;
    }

    @Getter
    @Setter
    public static class ActiveDialogue {
        private final String dialogueId;
        private final HappyNPC npc;
        private int currentLine;
        private boolean lineCompleted;

        public ActiveDialogue(String dialogueId, HappyNPC npc, int currentLine) {
            this.dialogueId = dialogueId;
            this.npc = npc;
            this.currentLine = currentLine;
            this.lineCompleted = false;
        }
    }

    @Getter
    public static class LocalizedLine {
        private final String ukrainianText;
        private final String englishText;

        public LocalizedLine(String ukrainianText, String englishText) {
            this.ukrainianText = ukrainianText;
            this.englishText = englishText;
        }

        public String getText(LocaleManager.SupportedLocale locale) {
            return locale == LocaleManager.SupportedLocale.UKRAINIAN ? ukrainianText : englishText;
        }
    }
}