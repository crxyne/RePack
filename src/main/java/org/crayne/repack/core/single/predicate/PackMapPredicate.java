package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a map of predicates, mapped to a texture file
public class PackMapPredicate implements PackPredicate {

    @NotNull
    private final PredicateType type;

    @NotNull
    private final List<PackSimplePredicate> predicates;

    public PackMapPredicate(@NotNull final PredicateType type, @NotNull final Collection<PackSimplePredicate> predicates) {
        this.type = type;
        this.predicates = new ArrayList<>(predicates);
    }

    @NotNull
    public List<PackSimplePredicate> predicates() {
        return predicates;
    }

    @NotNull
    public PredicateType type() {
        return type;
    }

    @NotNull
    public String toString() {
        return "PackMapPredicate{" +
                "type=" + type +
                ", predicates=" + predicates +
                '}';
    }
}

