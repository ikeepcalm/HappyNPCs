package dev.ua.ikeepcalm.happyNPCs.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.npc.HappyNPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;

@Command(name = "happynpc", aliases = "hpc")
public class HappyNPCCommand {

    private final HappyNPCs plugin;

    public HappyNPCCommand(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "help")
    @Permission("happynpcs.command")
    public void help(@Context CommandSender sender) {
        sendHelpMessage(sender);
    }

    @Execute(name = "create")
    @Permission("happynpcs.create")
    public void create(@Context Player player, @Arg("id") String id, @Arg("name") String name, @Arg("type") EntityType entityType) {
        String npcName = name != null ? name.replace("_", " ") : "NPC";
        EntityType type = entityType != null ? entityType : EntityType.VILLAGER;

        HappyNPC npc = plugin.getNpcManager().createNPC(
                id,
                player.getLocation(),
                npcName,
                plugin.getMiniMessage().deserialize(npcName)
        );

        if (npc == null) {
            player.sendMessage(Component.text("An NPC with ID '" + id + "' already exists.").color(NamedTextColor.RED));
            return;
        }

        npc.setEntityType(type);

        player.sendMessage(Component.text("Created NPC ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(id).color(NamedTextColor.GOLD))
                .append(Component.text(" with name "))
                .append(Component.text(npcName).color(NamedTextColor.GOLD)));
    }

    @Execute(name = "spawnmm")
    @Permission("happynpcs.create")
    public void spawnMythicMob(@Context Player player, @Arg("id") String id, @Arg("mobId") String mythicMobId, @Arg("name") String name) {
        if (!plugin.isMythicMobsAvailable()) {
            player.sendMessage(Component.text("MythicMobs is not installed on this server.").color(NamedTextColor.RED));
            return;
        }

        String npcName = name != null ? name.replace("_", " ") : "MythicNPC";

        HappyNPC npc = plugin.getNpcManager().createNPC(
                id,
                player.getLocation(),
                npcName,
                plugin.getMiniMessage().deserialize(npcName)
        );

        if (npc == null) {
            player.sendMessage(Component.text("An NPC with ID '" + id + "' already exists.").color(NamedTextColor.RED));
            return;
        }

        npc.setMythicMobId(mythicMobId);
        npc.despawn();
        npc.spawn();

        plugin.getNpcManager().saveNPCs();

        player.sendMessage(Component.text("Created MythicMob NPC ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(id).color(NamedTextColor.GOLD))
                .append(Component.text(" with mob type "))
                .append(Component.text(mythicMobId).color(NamedTextColor.GOLD)));
    }

    @Execute(name = "rotate")
    @Permission("happynpcs.move")
    public void rotate(@Context Player player, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().rotateNPC(id, player.getLocation().getYaw(), player.getLocation().getPitch());

        if (npc != null) {
            player.sendMessage(Component.text("Rotated NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD))
                    .append(Component.text(" to match your rotation.")));
        } else {
            player.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "rename")
    @Permission("happynpcs.create")
    public void rename(@Context CommandSender sender, @Arg("npcId") String id, @Join("name") String name) {
        String npcName = name != null ? name.replace("_", " ") : "NPC";

        HappyNPC npc = plugin.getNpcManager().renameNPC(id, npcName);

        if (npc != null) {
            sender.sendMessage(Component.text("Renamed NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD))
                    .append(Component.text(" to "))
                    .append(plugin.getMiniMessage().deserialize(npcName).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "remove")
    @Permission("happynpcs.remove")
    public void remove(@Context CommandSender sender, @Arg("npcId") String id) {
        boolean removed = plugin.getNpcManager().removeNPC(id);

        if (removed) {
            sender.sendMessage(Component.text("Removed NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "list")
    @Permission("happynpcs.command")
    public void list(@Context CommandSender sender) {
        Collection<HappyNPC> npcs = plugin.getNpcManager().getAllNPCs();

        if (npcs.isEmpty()) {
            sender.sendMessage(Component.text("No NPCs found.").color(NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("NPCs (" + npcs.size() + "):").color(NamedTextColor.GREEN));

        for (HappyNPC npc : npcs) {
            Location loc = npc.getLocation();
            String locationStr = String.format("%.0f, %.0f, %.0f in %s",
                    loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());

            Component message = Component.text("• ").color(NamedTextColor.GRAY)
                    .append(Component.text(npc.getId()).color(NamedTextColor.GOLD))
                    .append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text(npc.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" (").color(NamedTextColor.GRAY))
                    .append(Component.text(locationStr).color(NamedTextColor.AQUA))
                    .append(Component.text(")").color(NamedTextColor.GRAY));

            sender.sendMessage(message);
        }
    }

    @Execute(name = "move")
    @Permission("happynpcs.move")
    public void move(@Context Player player, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().moveNPC(id, player.getLocation());

        if (npc != null) {
            player.sendMessage(Component.text("Moved NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD))
                    .append(Component.text(" to your location.")));
        } else {
            player.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "hide")
    @Permission("happynpcs.hide")
    public void hide(@Context CommandSender sender, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().hideNPC(id);

        if (npc != null) {
            sender.sendMessage(Component.text("Hidden NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "show")
    @Permission("happynpcs.show")
    public void show(@Context CommandSender sender, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().showNPC(id);

        if (npc != null) {
            sender.sendMessage(Component.text("Shown NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "protect")
    @Permission("happynpcs.protect")
    public void protect(@Context CommandSender sender, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().setNPCProtection(id, true);

        if (npc != null) {
            sender.sendMessage(Component.text("Protected NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "unprotect")
    @Permission("happynpcs.protect")
    public void unprotect(@Context CommandSender sender, @Arg("npcId") String id) {
        HappyNPC npc = plugin.getNpcManager().setNPCProtection(id, false);

        if (npc != null) {
            sender.sendMessage(Component.text("Unprotected NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "setdialogue")
    @Permission("happynpcs.setdialogue")
    public void setDialogue(@Context CommandSender sender, @Arg("npcId") String id, @Arg("dialogueId") String dialogueId) {
        HappyNPC npc = plugin.getNpcManager().setNPCDialogue(id, dialogueId);

        if (npc != null) {
            sender.sendMessage(Component.text("Set dialogue of NPC ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(id).color(NamedTextColor.GOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(dialogueId).color(NamedTextColor.GOLD)));
        } else {
            sender.sendMessage(Component.text("No NPC found with ID '" + id + "'.").color(NamedTextColor.RED));
        }
    }

    @Execute(name = "reload")
    @Permission("happynpcs.reload")
    public void reload(@Context CommandSender sender) {
        sender.sendMessage(Component.text("Reloading HappyNPCs...").color(NamedTextColor.YELLOW));
        plugin.reloadAll();
        sender.sendMessage(Component.text("HappyNPCs reloaded!").color(NamedTextColor.GREEN));
    }

    private void sendHelpMessage(@Context CommandSender sender) {
        Component header = Component.text("=== HappyNPCs Help ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);

        sender.sendMessage(header);
        sender.sendMessage(Component.text("/happynpc create <id> [name] [entityType]").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc remove <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc list").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc move <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc hide <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc show <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc protect <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc unprotect <id>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc setdialogue <id> <dialogueId>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/happynpc reload").color(NamedTextColor.YELLOW));
    }
}