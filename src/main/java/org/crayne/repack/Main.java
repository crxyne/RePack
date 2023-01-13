package org.crayne.repack;

import org.crayne.repack.conversion.MinecraftItem;
import org.jetbrains.annotations.NotNull;

public class Main {

    public static void main(@NotNull final String... args) {
        System.out.println(MinecraftItem.allMatching("*_helmet"));
        //final Optional<PackWorkspace> workspace = new PackWorkspaceBuilder().setup(new File("test-workspace"));
        //workspace.ifPresent(System.out::println);
    }

}