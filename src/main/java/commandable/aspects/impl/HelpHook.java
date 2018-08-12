package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.Help;
import commandable.util.HelpPage;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class HelpHook implements AnnotationHook<Help> {

    private volatile HelpPage help;

    @Override
    public void onConstruction(Help annotation, Method method) {
        try {
            help = annotation.value().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> preEvaluate(Help annotation, CommandContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> postEvaluate(Help annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }

    public HelpPage getHelp() {
        return help;
    }
}
