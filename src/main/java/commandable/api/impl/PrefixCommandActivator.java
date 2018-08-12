package commandable.api.impl;

import commandable.api.Activation;
import commandable.api.CommandActivator;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

/**
 * This is a simple command activator that searches for a dynamically generated prefix.
 */
public abstract class PrefixCommandActivator implements CommandActivator {

    /**
     * Binds a {@link PrefixCommandActivator} to a static prefix string.
     *
     * @param prefix The static string to use.
     * @return The generated {@link PrefixCommandActivator}.
     */
    public static PrefixCommandActivator staticPrefix(String prefix) {
        return new PrefixCommandActivator() {
            @Override
            public Mono<String> getPrefix(MessageCreateEvent context) {
                return Mono.just(prefix);
            }
        };
    }

    public abstract Mono<String> getPrefix(MessageCreateEvent context);

    @Override
    public Mono<Activation> checkCommand(MessageCreateEvent context) {
        return Mono.defer(() -> {
            Message msg = context.getMessage();
            if (!msg.getContent().isPresent())
                return Mono.just(Activation.fail());
            String content = msg.getContent().get();
            return getPrefix(context)
                    .filter(content::startsWith)
                    .map(p -> Tuples.of(p, content.replaceFirst(p, "")))
                    .map(t -> {
                        String trimmed = t.getT2().trim();
                        if (trimmed.isEmpty())
                            return Activation.fail();
                        String[] split = trimmed.split("\\s+", 1);
                        return Activation.success(t.getT1(), split[0], split.length == 1 || split[1].isEmpty() ? null : split[1]);
                    });
        });
    }
}
