package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.TextureType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a list of items/armor/elytra types mapped to a single certain texture file
public class PackMapAllPredicate {

    private final TextureType type;
    private final List<String> keys;
    private final String predicate;

    public PackMapAllPredicate(@NotNull final TextureType type, @NotNull final Collection<String> keys, @NotNull final String predicate) {
        this.type = type;
        this.keys = new ArrayList<>(keys);
        this.predicate = predicate;
    }

    public String predicate() {
        return predicate;
    }

    public List<String> keys() {
        return keys;
    }

    public TextureType type() {
        return type;
    }

}
