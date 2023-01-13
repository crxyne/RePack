package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a list of items/armor/elytra types mapped to a single certain texture file
public class PackMapAllPredicate implements PackPredicate {

    @NotNull
    private final PredicateType type;

    @NotNull
    private final List<String> keys;

    @NotNull
    private final String predicate;

    public PackMapAllPredicate(@NotNull final PredicateType type, @NotNull final Collection<String> keys, @NotNull final String predicate) {
        this.type = type;
        this.keys = new ArrayList<>(keys);
        this.predicate = predicate;
    }

    @NotNull
    public String predicate() {
        return predicate;
    }

    @NotNull
    public List<String> keys() {
        return keys;
    }

    @NotNull
    public PredicateType type() {
        return type;
    }

    @NotNull
    public String toString() {
        return "PackMapAllPredicate{" +
                "type=" + type +
                ", keys=" + keys +
                ", predicate='" + predicate + '\'' +
                '}';
    }
}
