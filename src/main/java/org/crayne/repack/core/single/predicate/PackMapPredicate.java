package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.TextureType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a map of predicates, mapped to a texture file
public class PackMapPredicate {

    @NotNull
    private final TextureType type;

    @NotNull
    private final List<PackPredicate> predicates;

    public PackMapPredicate(@NotNull final TextureType type, @NotNull final Collection<PackPredicate> predicates) {
        this.type = type;
        this.predicates = new ArrayList<>(predicates);
    }

    @NotNull
    public List<PackPredicate> predicates() {
        return predicates;
    }

    @NotNull
    public TextureType type() {
        return type;
    }

}

