package org.crayne.repack.core.single.predicate;

import org.jetbrains.annotations.NotNull;

// a single, simple predicate, e.g display.Name = "ipattern:*example*"
public class PackPredicate {

    @NotNull
    private final String key;

    @NotNull
    private final String predicate;

    public PackPredicate(@NotNull final String key, @NotNull final String predicate) {
        this.key = key;
        this.predicate = predicate;
    }

    @NotNull
    public String key() {
        return key;
    }

    @NotNull
    public String predicate() {
        return predicate;
    }

}
