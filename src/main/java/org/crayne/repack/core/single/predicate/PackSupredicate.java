package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.TextureType;
import org.jetbrains.annotations.NotNull;

// the kind of SETALL_PREDICATE, where you can set all certain types of items for example to a single texture
public class PackSupredicate {

    @NotNull
    private final TextureType type;

    @NotNull
    private final String predicate;

    public PackSupredicate(@NotNull final TextureType type, @NotNull final String predicate) {
        this.type = type;
        this.predicate = predicate;
    }

    @NotNull
    public TextureType type() {
        return type;
    }

    @NotNull
    public String predicate() {
        return predicate;
    }

}
