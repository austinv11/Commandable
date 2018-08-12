package commandable.api;

import commandable.annotations.Service;
import commandable.util.HelpPage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;

@Service
public interface CommandPlugin {

    String name();

    String version();

    Collection<CommandGroup> getCommandGroups();

    default Publisher<HelpPage> helpPages() {
        return Flux.fromIterable(getCommandGroups()).flatMap(CommandGroup::helpPages);
    }
}
