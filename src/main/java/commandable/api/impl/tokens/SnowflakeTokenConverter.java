package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;
import commandable.util.MiscUtil;
import discord4j.core.object.util.Snowflake;

@WireService(TokenConverter.class)
public class SnowflakeTokenConverter extends AbstractTokenConverter<Snowflake> {

    public SnowflakeTokenConverter() {
        super(Snowflake.class, (s, c) -> MiscUtil.readSnowflake(s));
    }
}
