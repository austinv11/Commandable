package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.DMOnly;
import discord4j.command.util.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class DMOnlyHook implements AnnotationHook<DMOnly> {

    @Override
    public void onConstruction(DMOnly annotation, Method method) {

    }

    @Override
    public Mono<Void> preEvaluate(DMOnly annotation, CommandContext context) {
        return Mono.just(context.getEvent())
                .map(MessageCreateEvent::getGuildId)
                .filter(it -> !it.isPresent())
                .switchIfEmpty(Mono.error(() -> new CommandException("This command can only be executed in DMs!")))
                .then();
    }

    @Override
    public Mono<Void> postEvaluate(DMOnly annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }
}
