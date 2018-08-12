package commandable.api;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;

public final class CommandContext {

    private final MessageCreateEvent event;
    private final Activation activation;

    private final Mono<Guild> guild;
    private final Mono<User> author;
    private final Mono<MessageChannel> channel;
    private final Mono<User> selfUser;
    private final Mono<Member> selfMember;

    public CommandContext(Activation activation, MessageCreateEvent event) {
        this.activation = activation;
        this.event = event;
        this.guild = event.getGuild().cache();
        this.author = event.getMessage().getAuthor().cache();
        this.channel = event.getMessage().getChannel().cache();
        this.selfUser = event.getClient().getSelf().cache();
        this.selfMember = this.guild.flatMap(g ->
                g.getClient().getMemberById(g.getId(), event.getClient().getSelfId().get())
        ).cache();
    }

    public MessageCreateEvent getEvent() {
        return event;
    }

    public boolean isInGuild() {
        return event.getGuildId().isPresent();
    }

    public Snowflake getGuildId() {
        return event.getGuildId().get();
    }

    public Member getAuthorAsMember() {
        return event.getMember().get();
    }

    public Mono<Guild> getGuild() {
        return guild;
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public Mono<User> getAuthor() {
        return author;
    }

    public Mono<MessageChannel> getChannel() {
        return channel;
    }

    public String getTotalContent() {
        return getMessage().getContent().get();
    }

    public Optional<String> getCanaryString() {
        return activation.getCanaryString();
    }

    public String getCommandCalled() {
        return activation.getDetectedCommand();
    }

    public Optional<String> getReducedContent() {
        return activation.getDetectedCommandInput();
    }

    public DiscordClient getClient() {
        return event.getClient();
    }

    public Snowflake getBotId() {
        return getClient().getSelfId().get();
    }

    public Mono<User> getBotUser() {
        return selfUser;
    }

    public Mono<Member> getSelfMember() {
        return selfMember;
    }
}
