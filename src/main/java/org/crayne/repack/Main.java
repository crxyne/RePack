package org.crayne.repack;

import org.crayne.repack.core.PackWorkspaceBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final PackWorkspaceBuilder builder = new PackWorkspaceBuilder();
        builder.setup(new File("test-workspace"));
    }

}