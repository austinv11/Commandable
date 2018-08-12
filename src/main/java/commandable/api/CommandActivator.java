package commandable.api;

import commandable.annotations.Service;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

@Service
public interface CommandActivator {

    Mono<Activation> checkCommand(MessageCreateEvent context);
}
