package org.crayne.repack;

import org.crayne.repack.core.PackWorkspace;
import org.crayne.repack.core.PackWorkspaceBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public class Main {

    public static void main(@NotNull final String... args) {
        final Optional<PackWorkspace> workspace = new PackWorkspaceBuilder().setup(new File("test-workspace"));
        workspace.ifPresent(System.out::println);
    }

}