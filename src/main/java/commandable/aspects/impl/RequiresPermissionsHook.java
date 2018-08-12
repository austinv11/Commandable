package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.RequiresPermissions;
import commandable.util.MiscUtil;
import commandable.util.MissingPermissionsException;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.util.PermissionSet;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RequiresPermissionsHook implements AnnotationHook<RequiresPermissions> {

    @Override
    public void onConstruction(RequiresPermissions annotation, Method method) {

    }

    @Override
    public Mono<Void> preEvaluate(RequiresPermissions annotation, CommandContext context) {
        if (!context.isInGuild())
            return Mono.empty();
        return context.getChannel()
                .flatMap(gc -> MiscUtil.effectivePermissions(context.getAuthorAsMember(), (GuildChannel) gc))
                .filter(set -> !PermissionSet.of(annotation.value()).subtract(set).isEmpty())
                .then(Mono.error(() -> new MissingPermissionsException("Missing required permissions: " + String.join(",", Arrays.stream(annotation.value()).map(Enum::toString).collect(Collectors.toList())))));
    }

    @Override
    public Mono<Void> postEvaluate(RequiresPermissions annotation, CommandContext context,
                                   @Nullable Object returnValue) {
        return Mono.empty();
    }
}
