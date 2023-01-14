package org.crayne.repack.core.single;

import org.jetbrains.annotations.NotNull;

public record PackVariable(@NotNull String name, @NotNull String value) {

    @NotNull
    public String toString() {
        return "PackVariable {" +
                "name = '" + name + '\'' +
                ", value = '" + value + '\'' +
                '}';
    }
}
