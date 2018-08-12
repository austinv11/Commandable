package commandable.util;

import discord4j.command.util.CommandException;

import javax.annotation.Nullable;

public class MissingPermissionsException extends CommandException {

    public MissingPermissionsException(@Nullable String response) {
        super(response);
    }
}
