package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import commandable.util.MiscUtil;
import discord4j.command.util.CommandException;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class RoleTokenConverter extends AbstractTokenConverter<Role> {

    public RoleTokenConverter() {
        super(Role.class, (s, c) -> {
            if (!c.isInGuild()) {
                throw new CommandException("Cannot handle role '" + s + "' in a DM context!");
            }

            DiscordClient client = c.getClient();
            if (s.startsWith("<@&") && s.endsWith(">")) {
                Snowflake id = MiscUtil.readSnowflake(s);
                return client.getRoleById(c.getGuildId(), id).block(); //TODO: Better solution
            } else { //Warning: This only searches locally
                try {
                    long lid = Long.parseUnsignedLong(s);
                    return client.getRoleById(c.getGuildId(), Snowflake.of(lid)).block();
                } catch (Exception e) {}

                return c.getGuild()
                        .flatMapMany(Guild::getRoles)
                        .filter(r -> r.getName().equalsIgnoreCase(s)).blockFirst();
            }
        });
    }
}
