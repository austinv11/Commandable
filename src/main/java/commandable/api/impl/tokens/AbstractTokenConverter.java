package commandable.api.impl.tokens;

import commandable.api.CommandContext;
import commandable.api.TokenConverter;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

public class AbstractTokenConverter<O> implements TokenConverter<O> {

    private final Class<O> clazz;
    private final BiFunction<String, CommandContext, O> converter;

    public AbstractTokenConverter(Class<O> clazz, BiFunction<String, CommandContext, O> converter) {
        this.clazz = clazz;
        this.converter = converter;
    }

    @Override
    public Class<O> outType() {
        return clazz;
    }

    @Override
    public Token<O> readNextToken(String content, int startIndex, CommandContext context) {
        int endIndex = content.indexOf(' ', startIndex+1);
        if (endIndex < 0)
            endIndex = content.length();
        int finalEndIndex = endIndex;
        return new Token<>() {
            @Override
            public String original() {
                return content.substring(startIndex(), endIndex());
            }

            @Override
            public O create() {
                return converter.apply(original(), context);
            }

            @Override
            public int startIndex() {
                return startIndex;
            }

            @Override
            public int endIndex() {
                return finalEndIndex + 1;
            }
        };
    }
}
