package commandable.api;

import commandable.annotations.Service;
import discord4j.command.util.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Service
public interface ErrorBinding<T extends CommandException> {

    Class<T> bound();

    Mono<Void> handle(MessageCreateEvent context, T error);
}
