package org.crayne.repack;

import org.crayne.repack.commandline.CommandLineInterface;
import org.jetbrains.annotations.NotNull;

public class Main {

    public static void main(@NotNull final String... args) {
        CommandLineInterface.handle(args);
    }

}
