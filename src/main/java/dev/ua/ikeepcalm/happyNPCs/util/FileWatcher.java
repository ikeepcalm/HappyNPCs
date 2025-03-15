package dev.ua.ikeepcalm.happyNPCs.util;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FileWatcher {

    private final HappyNPCs plugin;
    private WatchService watchService;
    private Map<WatchKey, Path> watchKeys = new HashMap<>();
    private boolean running = false;
    private Thread watcherThread;
    
    public FileWatcher(HappyNPCs plugin) {
        this.plugin = plugin;
    }
    
    public void startWatching() {
        try {
            watchService = FileSystems.getDefault().newWatchService();

            Path pluginFolder = plugin.getDataFolder().toPath();
            registerFolder(pluginFolder);

            Path dialoguesFolder = pluginFolder.resolve("dialogues");
            if (Files.exists(dialoguesFolder)) {
                registerFolder(dialoguesFolder);
            }
            
            running = true;

            watcherThread = new Thread(this::watchLoop);
            watcherThread.setDaemon(true);
            watcherThread.setName("HappyNPCs-FileWatcher");
            watcherThread.start();
            
            plugin.getLogger().info("File watcher started");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start file watcher", e);
        }
    }
    
    private void registerFolder(Path folder) throws IOException {
        WatchKey key = folder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                StandardWatchEventKinds.ENTRY_CREATE);
        watchKeys.put(key, folder);
    }
    
    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.poll(100, TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            Path dir = watchKeys.get(key);
            if (dir == null) {
                continue;
            }
            
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path fileName = pathEvent.context();
                Path fullPath = dir.resolve(fileName);
                
                String fileExt = getFileExtension(fullPath.toString());
                if (!"yml".equals(fileExt) && !"yaml".equals(fileExt)) {
                    continue;
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                if (dir.endsWith("dialogues")) {
                    handleDialogueFileChange(fullPath, fileName.toString());
                } else {
                    handleMainFileChange(fileName.toString());
                }
            }
            
            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
                
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    private void handleDialogueFileChange(Path fullPath, String fileName) {
        String dialogueId = fileName;
        if (dialogueId.endsWith(".yml")) {
            dialogueId = dialogueId.substring(0, dialogueId.length() - 4);
        }

        String finalDialogueId = dialogueId;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.sendConsoleMessage("<yellow>Detected changes in dialogue file: " + fileName + ". Reloading...</yellow>");
            try {
                File backupFile = new File(fullPath.toString() + ".backup");
                Files.copy(fullPath, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                plugin.getDialogueManager().loadDialogues();
                plugin.sendConsoleMessage("<green>Dialogue " + finalDialogueId + " reloaded successfully!</green>");
                
                backupFile.delete();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reload dialogue " + finalDialogueId, e);
                plugin.sendConsoleMessage("<red>Failed to reload dialogue " + finalDialogueId + ". Check console for errors.</red>");
            }
        });
    }
    
    private void handleMainFileChange(String fileName) {
        if (fileName.equalsIgnoreCase("config.yml")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.sendConsoleMessage("<yellow>Detected changes in config.yml. Reloading...</yellow>");
                try {
                    plugin.getConfigManager().loadConfig();
                    plugin.sendConsoleMessage("<green>Config reloaded successfully!</green>");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to reload config", e);
                    plugin.sendConsoleMessage("<red>Failed to reload config. Check console for errors.</red>");
                }
            });
        } else if (fileName.equalsIgnoreCase("npcs.yml")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.sendConsoleMessage("<yellow>Detected changes in npcs.yml. Reloading...</yellow>");
                try {
                    plugin.getNpcManager().loadNPCs();
                    plugin.sendConsoleMessage("<green>NPCs reloaded successfully!</green>");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to reload NPCs", e);
                    plugin.sendConsoleMessage("<red>Failed to reload NPCs. Check console for errors.</red>");
                }
            });
        }
    }
    
    public void stopWatching() {
        running = false;
        
        if (watcherThread != null) {
            watcherThread.interrupt();
            try {
                watcherThread.join(1000);
            } catch (InterruptedException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to stop watcher thread", e);
            }
        }
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close watch service", e);
            }
        }
        
        watchKeys.clear();
    }
    
    private String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
    }
}