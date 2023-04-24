package org.crayne.repack.conversion.match;

import org.crayne.repack.conversion.util.TextureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public record ItemTextureMatch(@NotNull String texture, @Nullable TextureType type, @NotNull Collection<String> items, boolean matchAll) implements ItemMatch {

}
