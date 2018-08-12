package commandable.api;

import commandable.util.HelpPage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface CommandResolver {

    void indexCommand(Command cmd);

    Mono<Command> resolveCommand(String name, CommandContext context);

    Publisher<HelpPage> helpPages();
}
