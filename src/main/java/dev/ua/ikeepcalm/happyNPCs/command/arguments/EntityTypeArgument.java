package dev.ua.ikeepcalm.happyNPCs.command.arguments;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class EntityTypeArgument extends ArgumentResolver<CommandSender, EntityType> {

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<EntityType> argument, SuggestionContext context) {
        return Arrays.stream(EntityType.values())
                .filter(EntityType::isSpawnable)
                .map(EntityType::name)
                .collect(SuggestionResult.collector());
    }

    @Override
    protected ParseResult<EntityType> parse(Invocation<CommandSender> invocation, Argument<EntityType> argument, String input) {
        try {
            return ParseResult.success(EntityType.valueOf(input.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ParseResult.failure("Invalid entity type: " + input);
        }
    }
}