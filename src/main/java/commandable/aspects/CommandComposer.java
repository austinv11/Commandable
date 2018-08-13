package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.api.Activation;
import commandable.api.Command;
import commandable.api.CommandContext;
import commandable.api.TokenConverter;
import commandable.aspects.impl.AutoHelpHook;
import commandable.aspects.impl.HelpHook;
import commandable.aspects.impl.NameHook;
import commandable.util.HelpPage;
import discord4j.command.util.CommandException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandComposer {

    private final static Map<Class<? extends AnnotationHook>, AnnotationHook> hooks = Collections.synchronizedMap(new WeakHashMap<>());

    private final static Collection<Class<?>> specialTypes = Collections.singleton(CommandContext.class);

    private static void assertCommandAnnotation(Collection<? extends AnnotatedElement> aes) {
        for (AnnotatedElement ae : aes) {
            if (!ae.isAnnotationPresent(commandable.aspects.Command.class))
                throw new IllegalArgumentException("@Command annotation not present! This may indicate a malformed command.");
        }
    }

    private static List<? extends Annotation> findBoundAnnotations(AnnotatedElement ae) {
        List<Annotation> annotations = new ArrayList<>();
        for (Annotation a : ae.getAnnotations()) {
            if (a.annotationType().isAnnotationPresent(BindLogic.class))
                annotations.add(a);
        }
        return annotations;
    }

    private static Map<Class<? extends Annotation>, Tuple2<? extends Annotation, AnnotationHook>> getHooks(AnnotatedElement ae) {
        Map<Class<? extends Annotation>, Tuple2<? extends Annotation, AnnotationHook>> hookMap = new HashMap<>();
        findBoundAnnotations(ae)
                .stream()
                .map(a -> Tuples.of(a, a.annotationType().getAnnotation(BindLogic.class).value()))
                .map(t -> Tuples.of(t.getT1(), hooks.computeIfAbsent(t.getT2(), c -> {
                    try {
                        return c.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }))).forEach(t -> hookMap.put(t.getT1().annotationType(), t));
        return hookMap;
    }

    private static String generateName(Member ae) {
        return ae.getName().toLowerCase();
    }

    public static Command composeCommand(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return composeCommand(clazz.newInstance(),
                Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(commandable.aspects.Command.class))
                        .sorted(((Comparator<Method>) (o1, o2) -> {
                            return Integer.compare(o1.getParameterCount(), o2.getParameterCount());
                        }).reversed())
                        .collect(Collectors.toList()));
    }

    public static Command composeCommand(Object instance, Method method) throws IllegalAccessException {
        return composeCommand(instance, Collections.singletonList(method));
    }

    private static int effectiveParamCount(Method m) {
        int params = 0;
        for (Class<?> p : m.getParameterTypes()) {
            if (!specialTypes.contains(p))
                params += 1;
        }
        return params;
    }

    //TODO: Optional + nullable handling?
    private static Command composeCommand(Object instance, List<Method> methods) throws IllegalAccessException {
        assertCommandAnnotation(methods);

        Map<Method, Map<Class<? extends Annotation>, Tuple2<? extends Annotation, AnnotationHook>>> hooks
                = new HashMap<>();
        for (Method m : methods) {
            hooks.put(m, getHooks(m));
            hooks.get(m).values().forEach(t -> t.getT2().onConstruction(t.getT1(), m));
        }

        String name = null;
        List<HelpPage> help = new ArrayList<>();
        for (Method m : methods) {
            Map<Class<? extends Annotation>, Tuple2<? extends Annotation, AnnotationHook>> annotations = hooks.get(m);
            if (annotations.containsKey(Name.class)) {
                String n = ((NameHook) annotations.get(Name.class).getT2()).getName();
                if (name == null)
                    name = n;
                else if (!name.equals(n))
                    throw new RuntimeException("Inconsistent naming!");
            } else {
                String n = generateName(m);
                if (name == null)
                    name = n;
                else if (!name.equals(n))
                    throw new RuntimeException("Inconsistent naming!");
            }

            if (annotations.containsKey(Help.class)) {
                help.add(((HelpHook) annotations.get(Help.class).getT2()).getHelp());
            } else {
                if (annotations.containsKey(AutoHelp.class)) {

                    String desc = ((AutoHelp) annotations.get(AutoHelp.class).getT1()).value();
                    if (desc.isEmpty())
                        desc = "No description";
                    List<HelpPage.Argument> args = new ArrayList<>();
                    for (Parameter p : m.getParameters()) {
                        if (specialTypes.contains(p.getType()))
                            continue;

                        String pName;
                        if (p.isAnnotationPresent(AutoHelp.ParameterName.class)) {
                            pName = p.getAnnotation(AutoHelp.ParameterName.class).value();
                        } else {
                            pName = p.getName();
                        }

                        String typeName = p.getType().getSimpleName();
                        args.add(new HelpPage.Argument(typeName, false, pName));
                    }
                    help.add(new HelpPage(name, desc, Collections.singletonList(args)));
                } else
                    help.add(new HelpPage(name, "Cannot determine help information!", new ArrayList<>()));
            }
        }

        final String finalName = name;
        final HelpPage[] helpPages = help.toArray(new HelpPage[0]);
        return new Command() {

            private final Map<Method, MethodHandle> handles;

            {
                handles = new HashMap<>();
                for (Method m : methods)
                    handles.put(m, MethodHandles.lookup().unreflect(m).bindTo(instance));
            }

            @Override
            public String name() {
                return finalName;
            }

            @Override
            public HelpPage[] help() {
                return helpPages;
            }

            @Override
            public Mono<Void> checkPermissions(CommandContext context) {
                return Mono.empty(); //Handled later
            }

            @Override
            public discord4j.command.Command forDiscord4J(Activation activation, Map<Class<?>, TokenConverter<?>> tokenConverters,
                                                          CommandContext context) {
                return (event, ignored) -> {

                    for (Method method : methods) {
                        Map<Class<? extends Annotation>, Tuple2<? extends Annotation, AnnotationHook>> annotations
                                = hooks.get(method);
                        if (method.getParameterCount() < 1) {
                            if (activation.getDetectedCommandInput().isPresent()) {
                                throw new CommandException("Not expecting command arguments `" + activation.getDetectedCommandInput().get() + "`!");
                            } else {
                                return Flux.fromIterable(annotations.values())
                                        .flatMap(t -> (Mono<Tuple2<? extends Annotation, AnnotationHook>>) t.getT2().preEvaluate(t.getT1(), context).then(Mono.just(t)))
                                        .then(Mono.defer(() -> {
                                            try {
                                                Object o = handles.get(method).invokeWithArguments();
                                                if (o instanceof Publisher)
                                                    return Flux.from((Publisher<?>) o).then(Mono.justOrEmpty(Optional.of(o)));
                                                return Mono.just(Optional.ofNullable(o));
                                            } catch (Throwable throwable) {
                                                return Mono.error(throwable);
                                            }
                                        }))
                                        .cache()
                                        .flux()
                                        .zipWith(Flux.fromIterable(annotations.values()))
                                        .flatMap(t -> t.getT2().getT2().postEvaluate(t.getT2().getT1(), context, t.getT1()))
                                        .then();
                            }
                        }


                        if (effectiveParamCount(method) > activation.getDetectedCommandInput().orElse("").split(" ").length)
                            continue;

                        List<Object> converted = new ArrayList<>();
                        int startIndex = 0;
                        for (Parameter p : method.getParameters()) {
                            if (CommandContext.class.isAssignableFrom(p.getType())) {
                                converted.add(context);
                            } else if (Activation.class.isAssignableFrom(p.getType())) {
                                converted.add(activation);
                            } else if (tokenConverters.containsKey(p.getType())) {
                                TokenConverter<?> converter = tokenConverters.get(p.getType());
                                TokenConverter.Token<?> token = converter.readNextToken(activation.getDetectedCommandInput().get(), startIndex, context);
                                startIndex = token.endIndex();
                                converted.add(token);
                            } else {
                                throw new RuntimeException("Unable to bind parameter " + p);
                            }
                        }
                        return Flux.fromIterable(annotations.values())
                                .flatMap(t -> (Mono<Tuple2<? extends Annotation, AnnotationHook>>) t.getT2().preEvaluate(t.getT1(), context).then(Mono.just(t)))
                                .then(Mono.defer(() -> {
                                    try {
                                        Object o = handles.get(method).invokeWithArguments(converted);
                                        if (o instanceof Publisher)
                                            return Flux.from((Publisher<?>) o).then(Mono.justOrEmpty(Optional.of(o)));
                                        return Mono.just(Optional.ofNullable(o));
                                    } catch (Throwable throwable) {
                                        return Mono.error(throwable);
                                    }
                                }))
                                .cache()
                                .flux()
                                .zipWith(Flux.fromIterable(annotations.values()))
                                .flatMap(t -> t.getT2().getT2().postEvaluate(t.getT2().getT1(), context, t.getT1()))
                                .then();
                    }

                    throw new CommandException("Unable bind command to an executor!");
                };
            }
        };
    }
}
