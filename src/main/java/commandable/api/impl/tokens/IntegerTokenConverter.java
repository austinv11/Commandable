package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;

@WireService(TokenConverter.class)
public class IntegerTokenConverter extends AbstractTokenConverter<Integer> {

    public IntegerTokenConverter() {
        super(Integer.class, (s, c) -> Integer.parseInt(s));
    }
}
