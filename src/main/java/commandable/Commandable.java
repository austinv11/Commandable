package commandable;

import commandable.api.*;
import commandable.api.impl.CoreCommandPlugin;
import commandable.util.HelpPage;
import discord4j.command.*;
import discord4j.command.Command;
import discord4j.command.util.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public final class Commandable {

    private final static Logger log = Loggers.getLogger(Commandable.class);

    public static Set<CommandPlugin> findCommandPlugins() {
        ServiceLoader<CommandPlugin> pluginServices = ServiceLoader.load(CommandPlugin.class);
        Set<CommandPlugin> plugins = pluginServices.stream().map(ServiceLoader.Provider::get).collect(Collectors.toSet());
        plugins.add(new CoreCommandPlugin(new CopyOnWriteArraySet<>(plugins), findHelpPageRenderer()));
        return plugins;
    }

    public static Set<CommandGroup> findCommandGroups() {
        return findCommandGroups(findCommandPlugins());
    }

    public static Set<CommandGroup> findCommandGroups(Set<CommandPlugin> plugins) {
        return plugins.stream()
                .map(CommandPlugin::getCommandGroups)
                .filter(plugin -> !CoreCommandPlugin.class.isAssignableFrom(plugin.getClass()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static HelpPageRenderer findHelpPageRenderer() {
        ServiceLoader<HelpPageRenderer> helpPageRenderers = ServiceLoader.load(HelpPageRenderer.class);
        Optional<HelpPageRenderer> renderer = helpPageRenderers.findFirst();
        return renderer.orElseGet(() -> new HelpPageRenderer() {
            //TODO: make more robust?
            @Override
            public List<MessageCreateSpec> handleNew(List<HelpPage> pages) {
                return Collections.singletonList(new MessageCreateSpec()
                        .setContent(pages.stream()
                                .map(HelpPage::toString)
                                .distinct()
                                .collect(Collectors.joining("\n"))));
            }

            @Override
            public List<MessageEditSpec> handleEdit(List<HelpPage> pages) {
                return Collections.singletonList(new MessageEditSpec()
                        .setContent(pages.stream()
                                .map(HelpPage::toString)
                                .distinct()
                                .collect(Collectors.joining("\n"))));
            }

            @Override
            public MessageCreateSpec handleNew(HelpPage page) {
                return new MessageCreateSpec().setContent(page.toString(false));
            }

            @Override
            public MessageEditSpec handleEdit(HelpPage page) {
                return new MessageEditSpec().setContent(page.toString(false));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static CommandErrorHandler createErrorHandler() {
        ServiceLoader<ErrorBinding> bindings = ServiceLoader.load(ErrorBinding.class);
        Map<Class<? extends CommandException>, ErrorBinding> bindingMap = new ConcurrentHashMap<>();
        bindings.forEach(eb -> bindingMap.put(eb.bound(), eb));
        return (context, error) -> {
            if (!(error instanceof CommandException)) {
                return Mono.error(error);
            }
            if (bindingMap.containsKey(error.getClass())) {
                return bindingMap.get(error.getClass()).handle(context, (CommandException) error);
            } else {
                Optional<String> response = ((CommandException) error).response().map(s -> ":warning: " + s);
                if (response.isPresent()) {
                    MessageCreateSpec errorSpec = new MessageCreateSpec()
                            .setContent(response.get());
                    return context.getMember().map(member -> member.getPrivateChannel()
                                .flatMap(pc -> pc.createMessage(errorSpec))
                                .then())
                            .orElseGet(() -> context.getMessage()
                                .getChannel()
                                .flatMap(pc -> pc.createMessage(errorSpec))
                                .then());
                } else {
                    return context.getMessage().addReaction(ReactionEmoji.unicode("⚠"));
                }
            }
        };
    }

    public static Set<CommandActivator> findActivators() {
        ServiceLoader<CommandActivator> activators = ServiceLoader.load(CommandActivator.class);
        return activators.stream().map(ServiceLoader.Provider::get).collect(Collectors.toSet());
    }

    public static Map<Class<?>, TokenConverter<?>> findTokenConverters() {
        ServiceLoader<TokenConverter> activators = ServiceLoader.load(TokenConverter.class);
        Map<Class<?>, TokenConverter<?>> converterMap = new HashMap<>();
        activators.stream().map(ServiceLoader.Provider::get).forEach(tc -> converterMap.put(tc.outType(), tc));
        return converterMap;
    }

    public static CommandDispatcher createDispatcher() {
        return new CommandDispatcher() { //TODO: Custom dispatcher for when we add more advanced command discovery features
            final Set<CommandActivator> activators = findActivators();
            final Map<Class<?>, TokenConverter<?>> converters = findTokenConverters();

            @Override
            public Publisher<? extends Command<?>> dispatch(MessageCreateEvent event, Set<CommandProvider<?>> providers,
                                                            CommandErrorHandler errorHandler) {
                return event.getMessage()
                        .getAuthor()
                        .filter(u -> !u.isBot()) //Ignore bots
                        .then(Mono.defer(() -> Flux.fromIterable(activators)
                                .flatMap(a -> a.checkCommand(event))
                                .filter(Activation::isActivated)
                                .next()))
                        .flatMap(a -> {
                            //Copied from DefaultCommandDispatcher. This let's us hook in some extra stuff
                            return Flux.fromIterable(providers)
                                    .flatMap(provider -> {
                                        if (provider instanceof CommandGroup)
                                            return ((CommandGroup) provider).provide(a, converters, event); //Non-standard hook for cheaper lookups
                                        else
                                            return provider.provide(event, a.getDetectedCommand(), 0, //FIXME: real parsing?
                                                    event.getMessage().getContent().get().length());
                                    })
                                    .next()
                                    .map(pc -> (ProviderContext<Object>) pc) // IDEK
                                    .flatMap(ctx -> ctx.getCommand()
                                            .execute(event, ctx.getContext().orElse(null))
                                            .thenReturn(ctx.getCommand()))
                                    .doOnError(CommandException.class, throwable -> errorHandler.handle(event, throwable))
                                    .doOnError(t -> log.warn("Swallowing Exception", t))
                                    .onErrorResume(t -> Mono.empty());
                        });
            }
        };
    }
}
