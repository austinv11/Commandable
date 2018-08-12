package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.OwnerOnly;
import discord4j.command.util.CommandException;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class OwnerOnlyHook implements AnnotationHook<OwnerOnly> {

    private final static AtomicReference<Snowflake> owner = new AtomicReference<>(null);

    @Override
    public void onConstruction(OwnerOnly annotation, Method method) {

    }

    @Override
    public Mono<Void> preEvaluate(OwnerOnly annotation, CommandContext context) {
        return Mono.just(context.getClient())
                .flatMap(c -> {
                    if (owner.get() == null)
                        return c.getApplicationInfo();
                    else
                        return Mono.empty();
                })
                .map(ApplicationInfo::getOwnerId)
                .switchIfEmpty(Mono.defer(() -> Mono.just(owner.get())))
                .filter(it -> it.equals(context.getMessage().getAuthorId().orElse(null)))
                .switchIfEmpty(Mono.error(() -> new CommandException("This can only be executed by my owner!")))
                .then();
    }

    @Override
    public Mono<Void> postEvaluate(OwnerOnly annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }
}
