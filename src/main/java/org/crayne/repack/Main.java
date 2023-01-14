package org.crayne.repack;

import org.crayne.repack.conversion.PackWorkspace;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final boolean success = PackWorkspace.of(new File("test-workspace"))
                .map(p -> p.compile(new File("test-out")))
                .orElse(false);

        if (!success) System.exit(1);
    }

}