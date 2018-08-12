package commandable.test;

import commandable.api.CommandGroup;
import commandable.aspects.CommandComposer;

public class TestGroup extends CommandGroup {

    public TestGroup() {
        super();
        try {
            this.addCommand(CommandComposer.composeCommand(TestCommand.class));
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String name() {
        return "Test Group";
    }
}
