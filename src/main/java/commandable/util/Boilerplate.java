package commandable.util;

import commandable.Commandable;
import discord4j.command.CommandBootstrapper;
import discord4j.command.CommandProvider;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * No need to write boilerplate if you can just import it!
 */
public class Boilerplate {

    private final static Logger log = Loggers.getLogger(Boilerplate.class);

    private final String token;

    public static void main(String[] args) { //TODO add to MANIFEST.MF
        Boilerplate.from(args).launch(); //Super simple
    }

    /**
     * Constructs a boilerplate manager from an array of arguments.
     *
     * @param args The arguments, it is expected that the first item is the bot token.
     * @return The boilerplate instance.
     */
    public static Boilerplate from(String[] args) {
        if (args.length < 1)
            throw new IllegalArgumentException("Token not specified!");

        return from(args[0]);
    }

    /**
     * Constructs a boilerplate manager from a token.
     *
     * @param token The bot token to use.
     * @return The boilerplate instance.
     */
    public static Boilerplate from(String token) {
        return new Boilerplate(token);
    }

    private Boilerplate(String token) {
        this.token = token;
    }

    /**
     * Launches the bot. This does a few things:
     * <ol>
     *     <li>Ignores unknown json objects.</li>
     *     <li>Injects all found Commandable commands.</li>
     *     <li>Injects Commandable handlers.</li>
     *     <li>Creates a lightweight thread to keep the JVM alive without blocking (makes use of the Java 9 feature,
     *     {@link Thread#onSpinWait()})</li>
     *     <li>Cleans up resources on logout.</li>
     * </ol>
     *
     * @return The generated {@link discord4j.core.DiscordClient} instance (which is hot!).
     */
    public DiscordClient launch() {
        DiscordClient client = new DiscordClientBuilder(token)
                .setIgnoreUnknownJsonKeys(true)
                .build();
        CommandBootstrapper bootstrapper = new CommandBootstrapper(Commandable.createDispatcher(),
                Commandable.createErrorHandler());
        Commandable.findCommandGroups()
                .stream()
                .map(cg -> (CommandProvider) cg)
                .forEach(bootstrapper::addProvider);
        Disposable commands = bootstrapper.attach(client).subscribe();
        Thread watcher = new Thread(new ThreadGroup("Overwatch"), () -> {
            final AtomicBoolean disconnected = new AtomicBoolean(false);
            client.login().onErrorResume(t -> {
                log.error("Error caught! Closing...", t);
                return Mono.empty();
            }).then(Mono.just(commands))
                    .doOnNext(i -> disconnected.set(true)).subscribe(Disposable::dispose);
            while (!disconnected.get()) {
                Thread.onSpinWait();
            }
        }, "DiscordClient Keep-Alive", 1L, false);
        watcher.setDaemon(false);
        watcher.setPriority(Thread.MIN_PRIORITY);
        watcher.start();
        return client;
    }
}
