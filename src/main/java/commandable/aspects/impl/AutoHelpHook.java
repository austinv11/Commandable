package commandable.aspects.impl;

import commandable.api.CommandContext;
import commandable.aspects.AnnotationHook;
import commandable.aspects.AutoHelp;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

//Dummy, logic is handed in CommandComposer
public class AutoHelpHook implements AnnotationHook<AutoHelp> {

    @Override
    public void onConstruction(AutoHelp annotation, Method method) {

    }

    @Override
    public Mono<Void> preEvaluate(AutoHelp annotation, CommandContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> postEvaluate(AutoHelp annotation, CommandContext context, @Nullable Object returnValue) {
        return Mono.empty();
    }
}
