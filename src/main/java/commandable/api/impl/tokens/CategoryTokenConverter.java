package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import commandable.util.MiscUtil;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class CategoryTokenConverter extends AbstractTokenConverter<Category> {

    public CategoryTokenConverter() {
        super(Category.class, (s, c) -> {
            DiscordClient client = c.getClient();
            if (s.startsWith("<#") && s.endsWith(">")) {
                Snowflake id = MiscUtil.readSnowflake(s);
                return client.getCategoryById(id).block(); //TODO: Better solution
            } else { //Warning: This only searches locally
                try {
                    long lid = Long.parseUnsignedLong(s);
                    return client.getCategoryById(Snowflake.of(lid)).block();
                } catch (Exception e) {}

                return c.getGuild()
                        .flatMapMany(Guild::getChannels)
                        .ofType(Category.class)
                        .filter(c2 -> c2.getName().equalsIgnoreCase(s)).blockFirst();
            }
        });
    }
}
