package commandable.test;

import commandable.api.CommandContext;
import commandable.aspects.*;
import reactor.core.publisher.Mono;

@Command
public class TestCommand {

    @Command
    @Name("test")
    @OwnerOnly
    @AutoHelp
    public Mono<Void> test(CommandContext cxt) {
        return cxt.getChannel().flatMap(it -> it.createMessage("testing!!!!").then());
    }
}
