package commandable.util;

import java.util.List;
import java.util.stream.Collectors;

public class HelpPage {

    private final String commandName;
    private final String description;
    private final List<List<Argument>> arguments;

    public HelpPage(String commandName, String description, List<List<Argument>> arguments) {
        this.commandName = commandName;
        this.description = description;
        this.arguments = arguments;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getDescription() {
        return description;
    }

    public List<List<Argument>> getArguments() {
        return arguments;
    }

    public static class Argument {

        private final String typeString;
        private final boolean isOptional;
        private final String name;

        public Argument(String typeString, boolean isOptional, String name) {
            this.typeString = typeString;
            this.isOptional = isOptional;
            this.name = name;
        }

        public String getTypeString() {
            return typeString;
        }

        public boolean isOptional() {
            return isOptional;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            String s = getName() + ":" + getTypeString();
            if (isOptional)
                return "[" + s + "]";
            else
                return s;
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean condensed) {
        if (condensed) {
            return commandName.toLowerCase();
        } else {
            String header = "**" + commandName + "**\n";
            String desc = description + "\n";
            List<String> args = arguments.stream()
                    .map(as -> String.join(" ",
                            as.stream().map(Argument::toString).collect(Collectors.toList())))
                    .map(s -> "`" + commandName + " " + s + "`")
                    .collect(Collectors.toList());
            return header + desc + "**Syntax:**\n" + String.join("\n", args);
        }
    }
}
