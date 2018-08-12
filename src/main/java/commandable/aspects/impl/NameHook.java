package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.Name;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class NameHook implements AnnotationHook<Name> {

    private volatile String name;

    @Override
    public void onConstruction(Name annotation, Method method) {
        this.name = annotation.value();
    }

    @Override
    public Mono<Void> preEvaluate(Name annotation, CommandContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> postEvaluate(Name annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }

    public String getName() {
        return name;
    }
}
