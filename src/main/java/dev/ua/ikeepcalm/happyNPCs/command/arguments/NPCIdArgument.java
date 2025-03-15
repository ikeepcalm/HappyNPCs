package dev.ua.ikeepcalm.happyNPCs.command.arguments;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import org.bukkit.command.CommandSender;

public class NPCIdArgument extends ArgumentResolver<CommandSender, String> {

    private final HappyNPCs plugin;

    public NPCIdArgument(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<String> argument, SuggestionContext context) {
        return plugin.getNpcManager().getNPCMap().keySet().stream()
                .collect(SuggestionResult.collector());
    }

    @Override
    protected ParseResult<String> parse(Invocation<CommandSender> invocation, Argument<String> argument, String input) {
        return ParseResult.success(input);
    }
}