package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.TokenConverter;

@WireService(TokenConverter.class)
public class DoubleTokenConverter extends AbstractTokenConverter<Double> {

    public DoubleTokenConverter() {
        super(Double.class, (s, c) -> Double.parseDouble(s));
    }
}
