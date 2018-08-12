package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import commandable.util.MiscUtil;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class VoiceChannelTokenConverter extends AbstractTokenConverter<VoiceChannel> {

    public VoiceChannelTokenConverter() {
        super(VoiceChannel.class, (s, c) -> {
            DiscordClient client = c.getClient();
            if (s.startsWith("<#") && s.endsWith(">")) {
                Snowflake id = MiscUtil.readSnowflake(s);
                return client.getVoiceChannelById(id).block(); //TODO: Better solution
            } else { //Warning: This only searches locally
                try {
                    long lid = Long.parseUnsignedLong(s);
                    return client.getVoiceChannelById(Snowflake.of(lid)).block();
                } catch (Exception e) {}

                return c.getGuild()
                        .flatMapMany(Guild::getChannels)
                        .ofType(VoiceChannel.class)
                        .filter(c2 -> c2.getName().equalsIgnoreCase(s)).blockFirst();
            }
        });
    }
}
