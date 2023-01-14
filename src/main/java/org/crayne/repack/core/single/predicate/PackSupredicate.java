package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;

// the kind of SETALL_PREDICATE, where you can set all certain types of items for example to a single texture
public record PackSupredicate(@NotNull PredicateType type, @NotNull String predicate) implements PackPredicate {

    @NotNull
    public String toString() {
        return "PackSupredicate {" +
                "type = " + type +
                ", predicate = '" + predicate + '\'' +
                '}';
    }
}
