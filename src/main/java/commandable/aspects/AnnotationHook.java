package commandable.aspects;

import commandable.annotations.Service;
import commandable.api.CommandContext;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Service //Hooked up via @BindTo
public interface AnnotationHook<A extends Annotation> {

    void onConstruction(A annotation, Method method);

    Mono<Void> preEvaluate(A annotation, CommandContext context);

    Mono<Void> postEvaluate(A annotation, CommandContext context, @Nullable Object returnValue);
}
