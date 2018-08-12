package commandable.api;

import commandable.Commandable;
import commandable.api.impl.DefaultCommandResolver;
import commandable.util.HelpPage;
import discord4j.command.CommandProvider;
import discord4j.command.ProviderContext;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class CommandGroup implements CommandProvider {

    private final CommandResolver resolver;

    private final Set<CommandActivator> activators = new HashSet<>();
    private final Map<Class<?>, TokenConverter<?>> converters = new HashMap<>();

    public CommandGroup(CommandResolver resolver) {
        this.resolver = resolver;
    }

    public CommandGroup() {
        this(new DefaultCommandResolver());
    }

    public abstract String name();

    public CommandGroup addCommand(Command cmd) {
        resolver.indexCommand(cmd);
        return this;
    }

    public Publisher<HelpPage> helpPages() {
        return resolver.helpPages();
    }

    @Override
    public final Publisher<ProviderContext> provide(MessageCreateEvent context,
                                                       String commandName,
                                                       int startIndex,
                                                       int endIndex) {
        // We are compatible with the standard way of implementing CommandProviders, this just has some performance
        // effects due to repeated parsing instead of a shared Activation object for all groups
        // additionally we have a bit cleaner and more high level hint abstraction, so ignore the provided hints
        if (activators.isEmpty()) {
            activators.addAll(Commandable.findActivators());
        }
        if (converters.isEmpty()) {
            converters.putAll(Commandable.findTokenConverters());
        }
        return Mono.defer(() -> Flux.fromIterable(activators)
                .flatMap(a -> a.checkCommand(context))
                .filter(Activation::isActivated)
                .next()
        ).flatMap(a -> Mono.from(this.provide(a, converters, context)));
    }

    public Publisher<ProviderContext> provide(Activation activation, Map<Class<?>, TokenConverter<?>> converters,
                                                 MessageCreateEvent context) {
        CommandContext cc = new CommandContext(activation, context);
        return resolver.resolveCommand(activation.getDetectedCommand(), cc)
                .map(c -> c.forDiscord4J(activation, converters, cc))
                .map(ProviderContext::of);
    }
}
