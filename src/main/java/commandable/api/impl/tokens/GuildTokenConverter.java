package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class GuildTokenConverter extends AbstractTokenConverter<Guild> {

    public GuildTokenConverter() {
        super(Guild.class, (s, c) -> {
            DiscordClient client = c.getClient();
            try {
                long lid = Long.parseUnsignedLong(s);
                return client.getGuildById(Snowflake.of(lid)).block();
            } catch (Exception e) {}

            return client.getGuilds().filter(g -> g.getName().equalsIgnoreCase(s)).blockFirst();
        });
    }
}
