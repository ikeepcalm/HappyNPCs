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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

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

    public HappyNPC(String id, Location location, String name, Component displayName) {
        this.id = id;
        this.location = location;
        this.name = name;
        this.displayName = displayName;
    }

    public void spawn() {
        if (spawned && entity != null && !entity.isDead()) {
            return;
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
            entity.setMetadata("HappyNPC", new FixedMetadataValue(plugin, id));

            plugin.getNpcManager().updateNPCEntity(null, entity.getUniqueId(), this);

            spawned = true;
        }
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
        HappyNPCs.getInstance().getLogger().info("Attempting to spawn MythicMob entity with ID: " + mythicMobId);
        try {
            MythicPlugin mythicProvider = MythicProvider.get();
            MythicMob mythicMob = mythicProvider.getMobManager().getMythicMob(mythicMobId).orElse(null);

            if (mythicMob == null) {
                HappyNPCs.getInstance().getLogger().warning("MythicMob with ID '" + mythicMobId + "' not found. Spawning default entity.");
                entity = location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
                return;
            }

            AbstractLocation abstractLocation = new AbstractLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            ActiveMob activeMob = mythicMob.spawn(abstractLocation, 1, SpawnReason.OTHER);
            activeMob.setDisplayName(name);

            entity = activeMob.getEntity().getBukkitEntity();

            if (entity instanceof org.bukkit.entity.LivingEntity) {
                if (entity instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) entity).setRemoveWhenFarAway(false);
                }
            }

            entity.setMetadata("HappyNPC_MythicMob", new FixedMetadataValue(
                    HappyNPCs.getInstance(), activeMob));
        } catch (Exception e) {
            HappyNPCs.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Error spawning MythicMob", e);
            entity = location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
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
        if (entity != null && !entity.isDead()) {
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