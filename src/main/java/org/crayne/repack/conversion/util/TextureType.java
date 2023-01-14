package org.crayne.repack.conversion.util;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TextureType {

    ITEMS,
    ARMOR,
    ELYTRAS;

    @Nullable
    public static TextureType of(@NotNull final PredicateType type) {
        return switch (type) {
            case MATCH -> null;
            case ITEMS -> ITEMS;
            case ARMOR -> ARMOR;
            case ELYTRAS -> ELYTRAS;
        };
    }

    public String toString() {
        return switch (this) {
            case ARMOR -> "armor";
            case ITEMS -> "item";
            case ELYTRAS -> "elytra";
        };
    }
}
