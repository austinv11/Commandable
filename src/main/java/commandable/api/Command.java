package commandable.api;

import commandable.util.HelpPage;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

public interface Command  {

    String name();

    HelpPage[] help();

    Mono<Void> checkPermissions(CommandContext context);

    discord4j.command.Command forDiscord4J(Activation activation,
                                           Map<Class<?>, TokenConverter<?>> tokenConverters,
                                           CommandContext context);
}
