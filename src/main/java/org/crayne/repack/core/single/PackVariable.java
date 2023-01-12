package org.crayne.repack.core.single;

import org.jetbrains.annotations.NotNull;

public class PackVariable {


    @NotNull
    private final String name;

    @NotNull
    private final String value;

    public PackVariable(@NotNull final String name, @NotNull final String value) {
        this.name = name;
        this.value = value;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String value() {
        return value;
    }
}
