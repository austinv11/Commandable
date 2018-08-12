package commandable.api;

import javax.annotation.Nullable;
import java.util.Optional;

public final class Activation {

    private final boolean activated;
    private final @Nullable String canaryString;
    private final @Nullable String detectedCommand;
    private final @Nullable String detectedCommandInput;

    public static Activation fail() {
        return new Activation(false, null, null, null);
    }

    public static Activation success(@Nullable String canary, String command, @Nullable String remainingInput) {
        return new Activation(true, canary, command, remainingInput);
    }

    public Activation(boolean activated, @Nullable String canaryString, @Nullable String detectedCommand,
                      @Nullable String detectedCommandInput) {
        this.activated = activated;
        this.canaryString = canaryString;
        this.detectedCommand = detectedCommand;
        this.detectedCommandInput = detectedCommandInput;
    }

    public boolean isActivated() {
        return activated;
    }

    public Optional<String> getCanaryString() {
        return Optional.ofNullable(canaryString);
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    public String getDetectedCommand() {
        return detectedCommand;
    }

    public Optional<String> getDetectedCommandInput() {
        return Optional.ofNullable(detectedCommandInput == null || detectedCommandInput.isEmpty() ? null : detectedCommandInput);
    }
}
