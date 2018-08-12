package commandable.api.impl;

import commandable.api.Command;
import commandable.api.CommandContext;
import commandable.api.CommandResolver;
import commandable.util.HelpPage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCommandResolver implements CommandResolver {

    private final Map<String, Command> map = new ConcurrentHashMap<>();

    @Override
    public void indexCommand(Command cmd) {
        map.put(cmd.name(), cmd);
    }

    @Override
    public Mono<Command> resolveCommand(String name, CommandContext context) {
        return Mono.defer(() -> Mono.justOrEmpty(map.get(name)));
    }

    @Override
    public Publisher<HelpPage> helpPages() {
        return Flux.fromIterable(map.values()).map(Command::help).flatMap(Flux::fromArray);
    }
}
