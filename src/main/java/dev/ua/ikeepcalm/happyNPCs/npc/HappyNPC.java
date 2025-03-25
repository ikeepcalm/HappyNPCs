package dev.ua.ikeepcalm.happyNPCs.npc;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class HappyNPC {

    private final String id;
    private Location location;
    private String name;
    private final Component displayName;
    private Entity entity;
    private EntityType entityType = EntityType.VILLAGER;
    private boolean isProtected = true;
    private String dialogueId = "";
    private boolean spawned = false;
    private String mythicMobId = "";

    @Getter
    @Setter
    private UUID entityUUID;

    public HappyNPC(String id, Location location, String name, Component displayName) {
        this.id = id;
        this.location = location;
        this.name = name;
        this.displayName = displayName;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity != null && !entity.isDead() && !entity.getLocation().equals(location)) {
                    entity.teleport(location);
                }
            }
        }.runTaskTimer(HappyNPCs.getInstance(), 0L, 500L);
    }

    public void spawn() {
        if (entityUUID != null) {
            Entity existingEntity = findEntityByUUID(entityUUID);
            if (existingEntity != null && !existingEntity.isDead()) {
                entity = existingEntity;
                spawned = true;
                HappyNPCs.getInstance().getNpcManager().updateNPCEntity(null, entity.getUniqueId(), this);
                return;
            }
        }

        if (location.getWorld() != null) {
            for (Entity existingEntity : location.getWorld().getNearbyEntities(location, 20, 20, 20)) {
                PersistentDataContainer container = existingEntity.getPersistentDataContainer();
                if (container.has(new NamespacedKey(HappyNPCs.getInstance(), "HappyNPC"))) {
                    String npcId = container.get(new NamespacedKey(HappyNPCs.getInstance(), "HappyNPC"), PersistentDataType.STRING);
                    if (npcId != null && npcId.equals(this.id)) {
                        existingEntity.remove();
                    }
                }
            }
        }

        if (entity != null) {
            entity.remove();
            entity = null;
        }

        HappyNPCs plugin = HappyNPCs.getInstance();

        if (!mythicMobId.isEmpty() && plugin.isMythicMobsAvailable()) {
            spawnMythicMobEntity();
        } else {
            spawnVanillaEntity();
        }

        if (entity != null) {
            entity.customName(MiniMessage.miniMessage().deserialize(name));
            entity.setCustomNameVisible(true);
            entity.setPersistent(true);
            entity.setInvulnerable(isProtected);
            PersistentDataContainer container = entity.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "HappyNPC"), PersistentDataType.STRING, id);

            entityUUID = entity.getUniqueId();
            plugin.getNpcManager().updateNPCEntity(null, entityUUID, this);
            spawned = true;
        }
    }

    private Entity findEntityByUUID(UUID uuid) {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private void spawnVanillaEntity() {
        entity = location.getWorld().spawnEntity(location, entityType);

        entity.setPersistent(true);

        if (entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
            livingEntity.setRemoveWhenFarAway(false);
        }
    }

    public void setRotation(float yaw, float pitch) {
        this.location.setYaw(yaw);
        this.location.setPitch(pitch);

        if (entity != null && !entity.isDead()) {
            Location currentLoc = entity.getLocation();
            currentLoc.setYaw(yaw);
            currentLoc.setPitch(pitch);
            entity.teleport(currentLoc);
        }
    }

    public void setName(String name, Component displayName) {
        this.name = name;

        if (entity != null && !entity.isDead()) {
            entity.customName(displayName);
        }
    }

    private void spawnMythicMobEntity() {
        HappyNPCs plugin = HappyNPCs.getInstance();
        plugin.getLogger().info("Spawning MythicMob entity: " + mythicMobId + " for NPC: " + id);

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::spawnMythicMobEntity);
            return;
        }

        try {
            if (!plugin.isMythicMobsAvailable() || Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
                plugin.getLogger().warning("MythicMobs not available, using default entity");
                spawnVanillaEntity();
                return;
            }

            MythicPlugin mythicProvider = MythicProvider.get();
            if (mythicProvider == null || mythicProvider.getMobManager() == null) {
                plugin.getLogger().warning("MythicMobs API unavailable, using default entity");
                spawnVanillaEntity();
                return;
            }

            Optional<MythicMob> mythicMobOpt = mythicProvider.getMobManager().getMythicMob(mythicMobId);
            if (mythicMobOpt.isEmpty()) {
                plugin.getLogger().warning("MythicMob type '" + mythicMobId + "' not found");
                spawnVanillaEntity();
                return;
            }

            AbstractLocation abstractLocation = new AbstractLocation(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );

            ActiveMob activeMob = mythicMobOpt.get().spawn(abstractLocation, 1, SpawnReason.OTHER);

            if (activeMob == null || activeMob.getEntity() == null) {
                plugin.getLogger().warning("Failed to spawn MythicMob (null result)");
                spawnVanillaEntity();
                return;
            }

            entity = activeMob.getEntity().getBukkitEntity();

            entity.setMetadata("HappyNPC_MythicMob", new FixedMetadataValue(plugin, activeMob));

            if (entity instanceof org.bukkit.entity.Mob) {
                ((org.bukkit.entity.Mob) entity).setAI(false);
            }

            plugin.getLogger().info("Successfully spawned MythicMob for NPC: " + id);

        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error spawning MythicMob: " + e.getMessage(), e);
            spawnVanillaEntity();
        }
    }

    public void despawn() {
        if (entity != null) {
            entity.remove();
            entity = null;
        }

        spawned = false;
    }

    public void remove() {
        despawn();
    }

    public void teleport(Location location) {
        this.location = location;

        if (entity != null && !mythicMobId.isEmpty() && HappyNPCs.getInstance().isMythicMobsAvailable()) {
            despawn();
            this.location = location;
            spawn();
            return;
        }

        if (entity != null) {
            if (entity.isDead()) {
                spawn();
            }
            entity.teleport(location);
        }
    }

    public void showTo() {
        if (!spawned) {
            spawn();
        }
    }

    public Location getLocation() {
        return entity != null ? entity.getLocation() : location;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
        if (spawned) {
            despawn();
            spawn();
        }
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
        if (entity != null) {
            entity.setInvulnerable(isProtected);
        }
    }

    public boolean isSpawned() {
        return spawned && entity != null && !entity.isDead();
    }

    public void setMythicMobId(String mythicMobId) {
        this.mythicMobId = mythicMobId;
        HappyNPCs.getInstance().getLogger().info("MythicMob ID set to " + mythicMobId + " for NPC " + id);
        if (spawned && HappyNPCs.getInstance().isMythicMobsAvailable()) {
            despawn();
            spawnMythicMobEntity();
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HappyNPC npc = (HappyNPC) obj;
        return id.equals(npc.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}