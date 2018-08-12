package commandable.api;

import commandable.annotations.Service;
import reactor.core.publisher.Mono;

@Service
public interface TokenConverter<O> {

    Class<O> outType();

    Token<O> readNextToken(String content, int startIndex, CommandContext context);

    interface Token<O> {

        String original();

        O create();

        int startIndex();

        int endIndex();
    }
}
