package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// a single, simple predicate, e.g display.Name = "ipattern:*example*"
public record PackSimplePredicate(@NotNull String key, @NotNull String predicate, @Nullable PredicateType type) implements PackPredicate {

    @NotNull
    public String toString() {
        return "PackSimplePredicate{" +
                "key='" + key + '\'' +
                ", predicate='" + predicate + '\'' +
                ", type=" + type +
                '}';
    }
}
