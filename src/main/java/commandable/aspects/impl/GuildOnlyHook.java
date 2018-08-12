package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.GuildOnly;
import discord4j.command.util.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

public class GuildOnlyHook implements AnnotationHook<GuildOnly> {

    @Override
    public void onConstruction(GuildOnly annotation, Method method) {

    }

    @Override
    public Mono<Void> preEvaluate(GuildOnly annotation, CommandContext context) {
        return Mono.just(context.getEvent())
                .map(MessageCreateEvent::getGuildId)
                .filter(Optional::isPresent)
                .switchIfEmpty(Mono.error(() -> new CommandException("This command can only be executed in guilds!")))
                .then();
    }

    @Override
    public Mono<Void> postEvaluate(GuildOnly annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }
}
