package dev.ua.ikeepcalm.happyNPCs.command;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.argument.ArgumentKey;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import dev.ua.ikeepcalm.happyNPCs.command.arguments.DialogueIdArgument;
import dev.ua.ikeepcalm.happyNPCs.command.arguments.EntityTypeArgument;
import dev.ua.ikeepcalm.happyNPCs.command.arguments.MythicMobIdArgument;
import dev.ua.ikeepcalm.happyNPCs.command.arguments.NPCIdArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class CommandManager {

    private final HappyNPCs plugin;
    private LiteCommands<CommandSender> liteCommands;

    public CommandManager(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        this.liteCommands = LiteBukkitFactory.builder("Happy NPCs", plugin)
                .argument(String.class, ArgumentKey.of("npcId"), new NPCIdArgument(plugin))
                .argument(EntityType.class, new EntityTypeArgument())
                .argument(String.class, ArgumentKey.of("dialogueId"), new DialogueIdArgument(plugin))
                .argument(String.class, ArgumentKey.of("mobId"), new MythicMobIdArgument(plugin))
                .commands(new HappyNPCCommand(plugin))
                .build();
    }

    public void unregisterCommands() {
        if (liteCommands != null) {
            liteCommands.unregister();
        }
    }
}