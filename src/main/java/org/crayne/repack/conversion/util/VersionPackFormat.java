package org.crayne.repack.conversion.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum VersionPackFormat {

    V1_6_1(1),
    V1_6_2(1),
    V1_6_4(1),
    V1_7_2(1),
    V1_7_3(1),
    V1_7_4(1),
    V1_7_5(1),
    V1_7_6(1),
    V1_7_7(1),
    V1_7_8(1),
    V1_7_9(1),
    V1_7_10(1),
    V1_8(1),
    V1_8_1(1),
    V1_8_2(1),
    V1_8_3(1),
    V1_8_4(1),
    V1_8_5(1),
    V1_8_6(1),
    V1_8_7(1),
    V1_8_8(1),
    V1_8_9(1),

    V1_9(2),
    V1_9_1(2),
    V1_9_2(2),
    V1_9_3(2),
    V1_9_4(2),
    V1_10(2),
    V1_10_1(2),
    V1_10_2(2),

    V1_11(3),
    V1_11_1(3),
    V1_11_2(3),
    V1_12(3),
    V1_12_1(3),
    V1_12_2(3),

    V1_13(4),
    V1_13_1(4),
    V1_13_2(4),
    V1_14(4),
    V1_14_1(4),
    V1_14_2(4),
    V1_14_3(4),
    V1_14_4(4),

    V1_15(5),
    V1_15_1(5),
    V1_15_2(5),
    V1_16(5),
    V1_16_1(5),

    V1_16_2(6),
    V1_16_3(6),
    V1_16_4(6),
    V1_16_5(6),

    V1_17(7),
    V1_17_1(7),

    V1_18(8),
    V1_18_1(8),
    V1_18_2(8),

    V1_19(9),
    V1_19_1(9),
    V1_19_2(9),

    V1_19_3(12),

    V1_19_4(13);

    private final int packFormat;

    VersionPackFormat(final int packFormat) {
        this.packFormat = packFormat;
    }

    public int packFormat() {
        return packFormat;
    }

    public static Optional<VersionPackFormat> of(@NotNull final String str) {
        try {
            return Optional.of(valueOf((str.toUpperCase().startsWith("V") ? "" : "V") + str.toUpperCase()));
        } catch (final IllegalArgumentException e) {
            return Arrays.stream(values())
                    .filter(v -> v.name()
                            .substring(1)
                            .toLowerCase()
                            .replace("_", ".").
                            equals(str))
                    .findAny();
        }
    }

}
