package dev.ua.ikeepcalm.happyNPCs.command.arguments;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import org.bukkit.command.CommandSender;

public class MythicMobIdArgument extends ArgumentResolver<CommandSender, String> {

    private final HappyNPCs plugin;

    public MythicMobIdArgument(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<String> argument, SuggestionContext context) {
        if (!plugin.isMythicMobsAvailable()) {
            return SuggestionResult.empty();
        }

        try {
            MythicPlugin mythicProvider = MythicProvider.get();
            return mythicProvider.getMobManager().getMobTypes().stream()
                    .map(MythicMob::getInternalName)
                    .collect(SuggestionResult.collector());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get MythicMob types: " + e.getMessage());
            return SuggestionResult.empty();
        }
    }

    @Override
    protected ParseResult<String> parse(Invocation<CommandSender> invocation, Argument<String> argument, String input) {
        if (!plugin.isMythicMobsAvailable()) {
            return ParseResult.failure("MythicMobs is not installed on this server.");
        }

        try {
            MythicPlugin mythicProvider = MythicProvider.get();
            if (mythicProvider.getMobManager().getMythicMob(input).isPresent()) {
                return ParseResult.success(input);
            } else {
                return ParseResult.failure("MythicMob type '" + input + "' does not exist.");
            }
        } catch (Exception e) {
            return ParseResult.failure("Error checking MythicMob type: " + e.getMessage());
        }
    }
}