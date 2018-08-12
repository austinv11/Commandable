package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;

@WireService(TokenConverter.class)
public class LongTokenConverter extends AbstractTokenConverter<Long> {

    public LongTokenConverter() {
        super(Long.class, (s, c) -> Long.parseLong(s));
    }
}
