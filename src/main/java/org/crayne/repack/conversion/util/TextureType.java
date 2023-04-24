package org.crayne.repack.conversion.util;

import org.crayne.repack.core.single.PredicateType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TextureType {

    ITEMS,
    ARMOR,
    ARMOR_L1,
    ARMOR_L2,
    ELYTRAS;

    @Nullable
    public static TextureType of(@NotNull final PredicateType type) {
        return switch (type) {
            case ITEMS -> ITEMS;
            case ARMOR -> ARMOR;
            case ELYTRAS -> ELYTRAS;
            case ARMOR_L1 -> ARMOR_L1;
            case ARMOR_L2 -> ARMOR_L2;
            default -> null;
        };
    }

    public String toString() {
        return switch (this) {
            case ARMOR, ARMOR_L1, ARMOR_L2 -> "armor";
            case ITEMS -> "item";
            case ELYTRAS -> "elytra";
        };
    }
}
