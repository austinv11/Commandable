package commandable.api.impl.tokens;

import commandable.annotations.WireService;
import commandable.api.CommandContext;
import commandable.api.TokenConverter;
import reactor.core.publisher.Mono;

@WireService(TokenConverter.class)
public class StringTokenConverter implements TokenConverter<String> {

    @Override
    public Class<String> outType() {
        return String.class;
    }

    @Override
    public Token<String> readNextToken(String content, int startIndex, CommandContext context) {
        String s = content.substring(startIndex);
        if (s.startsWith("\"")) {
            int nextIndex = content.indexOf('"', startIndex+1);
            if (nextIndex > -1) {
                String token = content.substring(startIndex+1, nextIndex);
                return new StringToken("\"" + token + "\"", token, startIndex, nextIndex+1);
            }
        } else if (s.startsWith("'")) {
            int nextIndex = content.indexOf('\'', startIndex+1);
            if (nextIndex > -1) {
                String token = content.substring(startIndex+1, nextIndex);
                return new StringToken("'" + token + "'", token, startIndex, nextIndex+1);
            }
        }

        String firstWord = s.split(" ", 1)[0];
        return new StringToken(firstWord, firstWord, startIndex, firstWord.length()+1);
    }

    private final class StringToken implements Token<String> {

        private final String original;
        private final String stripped;
        private final int startIndex;
        private final int endIndex;

        private StringToken(String original, String stripped, int startIndex, int endIndex) {
            this.original = original;
            this.stripped = stripped;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public String original() {
            return original;
        }

        @Override
        public String create() {
            return stripped;
        }

        @Override
        public int startIndex() {
            return startIndex;
        }

        @Override
        public int endIndex() {
            return endIndex;
        }
    }
}
