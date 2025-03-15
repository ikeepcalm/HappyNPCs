package dev.ua.ikeepcalm.happyNPCs.command.arguments;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DialogueIdArgument extends ArgumentResolver<CommandSender, String> {

    private final HappyNPCs plugin;

    public DialogueIdArgument(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<String> argument, SuggestionContext context) {
        List<String> dialogueIds = new ArrayList<>();

        File dialoguesFolder = new File(plugin.getDataFolder(), "dialogues");
        if (dialoguesFolder.exists() && dialoguesFolder.isDirectory()) {
            File[] files = dialoguesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String dialogueId = file.getName().substring(0, file.getName().length() - 4); // Remove .yml
                    dialogueIds.add(dialogueId);
                }
            }
        }

        return dialogueIds.stream()
                .collect(SuggestionResult.collector());
    }

    @Override
    protected ParseResult<String> parse(Invocation<CommandSender> invocation, Argument<String> argument, String input) {
        File dialogueFile = new File(plugin.getDataFolder(), "dialogues/" + input + ".yml");
        if (!dialogueFile.exists()) {
            return ParseResult.failure("Dialogue '" + input + "' does not exist");
        }

        return ParseResult.success(input);
    }
}