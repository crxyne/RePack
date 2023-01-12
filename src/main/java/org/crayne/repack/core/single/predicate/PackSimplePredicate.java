package org.crayne.repack.core.single.predicate;

import org.jetbrains.annotations.NotNull;

// a single, simple predicate, e.g display.Name = "ipattern:*example*"
public record PackSimplePredicate(@NotNull String key, @NotNull String predicate) implements PackPredicate {

}
