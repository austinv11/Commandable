package commandable.api.impl;

import commandable.annotations.WireService;
import commandable.api.Activation;
import commandable.api.CommandActivator;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This {@link commandable.api.CommandActivator} implementation will detect commands triggered with the following
 * pattern: @BotMention COMMAND [args...]
 */
@WireService(CommandActivator.class) //Actually NO-OP in this case, but in case people use this as an example, it's included
public class MentionActivator implements CommandActivator {

    private static final String BASE_REGEX = "(?:<@!?)(%s)(?:>)(?=.*)";

    private final AtomicReference<Pattern> pattern = new AtomicReference<>();

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Mono<Activation> checkCommand(MessageCreateEvent context) {
        if (pattern.get() == null) {
            pattern.set(Pattern.compile(String.format(BASE_REGEX, context.getClient().getSelfId().get().asString())));
        }
        return Mono.defer(() -> {
            Message msg = context.getMessage();
            if (!msg.getContent().isPresent()
                    || !msg.getContent().get().startsWith("<@")
                    || msg.getContent().get().split(" ").length <= 1)
                return Mono.just(Activation.fail());
            String content = msg.getContent().get();
            Matcher matcher = pattern.get().matcher(content);
            if (!matcher.find(0))
                return Mono.just(Activation.fail());
            String match = matcher.group(1);
            if (match == null || match.isEmpty() || !context.getClient().getSelfId().get().equals(Snowflake.of(match)))
                return Mono.just(Activation.fail());
            String replaced = matcher.replaceFirst("");
            if (!replaced.startsWith(" "))
                return Mono.just(Activation.fail()); //Mention was not in the beginning
            replaced = replaced.substring(1);
            String[] split = replaced.split("\\s", 2);
            return Mono.just(Activation.success("<@" + match + ">", split[0], split.length == 1 ? null : split[1]));
        });
    }
}
