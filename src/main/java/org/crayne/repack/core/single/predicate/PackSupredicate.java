package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.TextureType;
import org.jetbrains.annotations.NotNull;

// the kind of SETALL_PREDICATE, where you can set all certain types of items for example to a single texture
public record PackSupredicate(@NotNull TextureType type, @NotNull String predicate) implements PackPredicate {

}
