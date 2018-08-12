package commandable.test;

import commandable.api.CommandGroup;
import commandable.api.CommandPlugin;

import java.util.Collection;
import java.util.Collections;

public class TestPlugin implements CommandPlugin {

    @Override
    public String name() {
        return "Test Plugin";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public Collection<CommandGroup> getCommandGroups() {
        return Collections.singleton(new TestGroup());
    }
}
