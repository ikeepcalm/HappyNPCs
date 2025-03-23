package dev.ua.ikeepcalm.happyNPCs.manager;


import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.logging.Level;

public class NPCManager {

    private final HappyNPCs plugin;
    private final Map<String, HappyNPC> npcs = new HashMap<>();
    private final Map<UUID, HappyNPC> entityIdToNPC = new HashMap<>();

    public NPCManager(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    public void loadNPCs() {
        removeAllNPCs();

        FileConfiguration npcsConfig = plugin.getConfigManager().getNpcsConfig();
        ConfigurationSection npcsSection = npcsConfig.getConfigurationSection("npcs");

        if (npcsSection == null) {
            plugin.getLogger().warning("No NPCs section found in npcs.yml. Creating empty section.");
            npcsConfig.createSection("npcs");
            return;
        }

        for (String npcId : npcsSection.getKeys(false)) {
            try {
                ConfigurationSection npcSection = npcsSection.getConfigurationSection(npcId);
                if (npcSection == null) continue;

                String worldName = npcSection.getString("location.world");
                double x = npcSection.getDouble("location.x");
                double y = npcSection.getDouble("location.y");
                double z = npcSection.getDouble("location.z");
                float yaw = (float) npcSection.getDouble("location.yaw");
                float pitch = (float) npcSection.getDouble("location.pitch");

                if (Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("World '" + worldName + "' not found for NPC " + npcId + ". Skipping.");
                    continue;
                }

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                String name = npcSection.getString("name", "NPC");
                String entityType = npcSection.getString("entityType", "PLAYER");
                boolean protected_ = npcSection.getBoolean("protected", true);
                String dialogueId = npcSection.getString("dialogueId", "");
                String mythicMobId = npcSection.getString("mythicMobId", "");

                HappyNPC npc = new HappyNPC(npcId, location, name, plugin.getMiniMessage().deserialize(name));
                npc.setProtected(protected_);
                npc.setDialogueId(dialogueId);

                try {
                    npc.setEntityType(EntityType.valueOf(entityType));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity type '" + entityType + "' for NPC " + npcId + ". Using PLAYER.");
                    npc.setEntityType(EntityType.VILLAGER);
                }

                if (plugin.isMythicMobsAvailable() && !mythicMobId.isEmpty()) {
                    npc.setMythicMobId(mythicMobId);
                }

                if (npcSection.getBoolean("spawned", true)) {
                    npc.spawn();
                }

                npcs.put(npcId, npc);
                if (npc.getEntity() != null) {
                    entityIdToNPC.put(npc.getEntity().getUniqueId(), npc);
                }

                plugin.getLogger().info("Loaded NPC " + npcId);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load NPC " + npcId, e);
            }
        }

        plugin.getLogger().info("Loaded " + npcs.size() + " NPCs");
    }

    public void saveNPCs() {
        FileConfiguration npcsConfig = plugin.getConfigManager().getNpcsConfig();
        ConfigurationSection npcsSection = npcsConfig.createSection("npcs");

        for (Map.Entry<String, HappyNPC> entry : npcs.entrySet()) {
            String npcId = entry.getKey();
            HappyNPC npc = entry.getValue();

            ConfigurationSection npcSection = npcsSection.createSection(npcId);

            Location loc = npc.getLocation();
            npcSection.set("location.world", loc.getWorld().getName());
            npcSection.set("location.x", loc.getX());
            npcSection.set("location.y", loc.getY());
            npcSection.set("location.z", loc.getZ());
            npcSection.set("location.yaw", loc.getYaw());
            npcSection.set("location.pitch", loc.getPitch());

            npcSection.set("name", npc.getName());
            npcSection.set("entityType", npc.getEntityType().toString());
            npcSection.set("protected", npc.isProtected());
            npcSection.set("dialogueId", npc.getDialogueId());
            npcSection.set("spawned", npc.isSpawned());

            if (npc.getMythicMobId() != null && !npc.getMythicMobId().isEmpty()) {
                npcSection.set("mythicMobId", npc.getMythicMobId());
            }
        }

        plugin.getConfigManager().saveNPCsConfig();
    }

    public HappyNPC rotateNPC(String id, float yaw, float pitch) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.setRotation(yaw, pitch);
        saveNPCs();
        return npc;
    }

    public HappyNPC renameNPC(String id, String newName) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        Component displayName = HappyNPCs.getInstance().getMiniMessage().deserialize(newName);
        npc.setName(newName, displayName);
        saveNPCs();
        return npc;
    }

    public HappyNPC createNPC(String id, Location location, String name, Component displayName) {
        if (npcs.containsKey(id)) {
            return null;
        }

        HappyNPC npc = new HappyNPC(id, location, name, displayName);
        npc.spawn();

        npcs.put(id, npc);
        if (npc.getEntity() != null) {
            entityIdToNPC.put(npc.getEntity().getUniqueId(), npc);
        }

        saveNPCs();

        return npc;
    }

    public boolean removeNPC(String id) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return false;
        }

        if (npc.getEntity() != null) {
            entityIdToNPC.remove(npc.getEntity().getUniqueId());
        }
        npcs.remove(id);
        npc.remove();
        saveNPCs();

        return true;
    }

    public void removeAllNPCs() {
        for (HappyNPC npc : new ArrayList<>(npcs.values())) {
            npc.remove();
        }

        npcs.clear();
        entityIdToNPC.clear();
    }

    public HappyNPC getNPCByEntity(Entity entity) {
        return entityIdToNPC.get(entity.getUniqueId());
    }

    public boolean isNPC(Entity entity) {
        return entityIdToNPC.containsKey(entity.getUniqueId());
    }

    public Collection<HappyNPC> getAllNPCs() {
        return npcs.values();
    }

    public HappyNPC moveNPC(String id, Location location) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.teleport(location);
        saveNPCs();
        return npc;
    }

    public HappyNPC hideNPC(String id) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.despawn();
        saveNPCs();
        return npc;
    }

    public HappyNPC showNPC(String id) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.spawn();
        saveNPCs();
        return npc;
    }

    public HappyNPC setNPCProtection(String id, boolean protected_) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.setProtected(protected_);
        saveNPCs();
        return npc;
    }

    public HappyNPC setNPCDialogue(String id, String dialogueId) {
        HappyNPC npc = npcs.get(id);
        if (npc == null) {
            return null;
        }

        npc.setDialogueId(dialogueId);
        saveNPCs();
        return npc;
    }

    public Map<String, HappyNPC> getNPCMap() {
        return new HashMap<>(npcs);
    }

    public void updateNPCEntity(UUID oldEntityId, UUID newEntityId, HappyNPC npc) {
        if (oldEntityId != null) {
            entityIdToNPC.remove(oldEntityId);
        }
        if (newEntityId != null) {
            entityIdToNPC.put(newEntityId, npc);
        }
    }

    public void showNPCsTo() {
        for (HappyNPC npc : npcs.values()) {
            if (npc.isSpawned()) {
                npc.showTo();
            }
        }
    }

    public void despawnAllNPCs() {
        for (HappyNPC npc : new ArrayList<>(npcs.values())) {
            npc.despawn();
        }
        entityIdToNPC.clear();
    }
}