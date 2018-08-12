package commandable.util;

import discord4j.command.util.CommandException;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtil {

    private static final Pattern ANY_MENTION_PATTERN = Pattern.compile("(?:<[@#][!&]?)(\\d+)(?:>)");

    @Nullable
    public static Snowflake readSnowflake(String s) {
        try {
            if (s.startsWith("<")) {
                Matcher m = ANY_MENTION_PATTERN.matcher(s);
                if (!m.matches())
                    return null;
                return Snowflake.of(m.group(1));
            } else {
                return Snowflake.of(s);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean nameMatches(Member m, String name, @Nullable String discrim) {
        boolean nameMatches = m.getDisplayName().equalsIgnoreCase(name) || m.getUsername().equalsIgnoreCase(name);
        if (discrim != null) {
            return nameMatches && m.getDiscriminator().equalsIgnoreCase(discrim);
        } else {
            return nameMatches;
        }
    }

    public static Flux<ReactionAddEvent> listenToReactions(Message m) {
        return m.getClient()
                .getEventDispatcher()
                .on(MessageEvent.class)
                .filter(e -> !(e instanceof MessageCreateEvent))
                .flatMap(e -> {
                    if (e instanceof MessageDeleteEvent) {
                        if (!((MessageDeleteEvent) e).getMessageId().equals(m.getId()))
                            return Mono.empty();
                        else
                            return Mono.error(new CommandException(null)); //Breaks the subscription
                    } else if (e instanceof ReactionAddEvent) {
                        ReactionAddEvent e2 = (ReactionAddEvent) e;
                        if (!e2.getMessageId().equals(m.getId()))
                            return Mono.empty();
                        else
                            return Mono.just(e2);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public static Mono<PermissionSet> effectivePermissions(Member m, @Nullable GuildChannel gc) {
        return m.getRoles().map(Role::getPermissions)
                .reduce(PermissionSet::or) // Got base permissions
                .zipWith(Mono.just(Optional.ofNullable(gc == null ? null : gc.getPermissionOverwrites())))
                .map(it -> {
                    PermissionSet base = it.getT1();
                    Set<PermissionOverwrite> overwrites = it.getT2().orElseGet(HashSet::new);
                    for (PermissionOverwrite po : overwrites) {
                        if ((po.getRoleId().isPresent() && m.getRoleIds().contains(po.getRoleId().get()))
                                || (po.getUserId().isPresent() && m.getId().equals(po.getUserId().get()))) {
                            base = base.or(po.getAllowed());
                            base = base.subtract(po.getDenied());
                        }
                    }
                    return base;
                })
                .map(set -> {
                    if (set.contains(Permission.ADMINISTRATOR))
                        return PermissionSet.all();
                    else
                        return set;
                });
    }
}
