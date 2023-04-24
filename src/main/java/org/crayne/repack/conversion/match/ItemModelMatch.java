package org.crayne.repack.conversion.match;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record ItemModelMatch(@NotNull String json, @NotNull PredicateType type, @NotNull Collection<String> items, boolean matchAll) implements ItemMatch {
}
