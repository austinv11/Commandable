package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import commandable.util.MiscUtil;
import discord4j.command.util.CommandException;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class UserTokenConverter extends AbstractTokenConverter<User> {

    public UserTokenConverter() {
        super(User.class, (s, c) -> {
            DiscordClient client = c.getClient();
            if (s.startsWith("<@") && s.endsWith(">")) {
                Snowflake id = MiscUtil.readSnowflake(s);
                return client.getUserById(id).block(); //TODO: Better solution
            } else { //Warning: This only searches locally
                if (!c.isInGuild()) { //FIXME
                    throw new CommandException("Cannot handle user '" + s + "' in a DM context!");
                }

                try {
                    long lid = Long.parseUnsignedLong(s);
                    return client.getUserById(Snowflake.of(lid)).block();
                } catch (Exception e) {}

                if (s.contains("#") && !s.endsWith("#")) {
                    try {
                        String name = s.substring(0, s.lastIndexOf("#"));
                        String discrim = s.substring(s.lastIndexOf("#")+1);
                        return c.getGuild()
                                .flatMapMany(Guild::getMembers)
                                .filter(m -> MiscUtil.nameMatches(m, name, discrim)).blockFirst();
                    } catch (Exception e) {}
                }
                return c.getGuild()
                        .flatMapMany(Guild::getMembers)
                        .filter(m -> MiscUtil.nameMatches(m, s, null)).blockFirst();
            }
        });
    }
}
