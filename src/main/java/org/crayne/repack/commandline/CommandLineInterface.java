package org.crayne.repack.commandline;

import org.crayne.repack.conversion.PackWorkspace;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandLineInterface {

    private static final Logger LOGGER = new Logger();

    private static void compile(@NotNull final String in, @NotNull final String out) {
        final boolean success = PackWorkspace
                .of(in)
                .map(p -> p.compile(out))
                .orElse(false);

        if (!success) System.exit(1);
    }

    private static boolean handleNoArguments(@NotNull final String... args) {
        if (args.length != 0) return false;

        LOGGER.error("No arguments provided. Use the 'help' argument to show the usage of RePack.");
        LOGGER.log("Usage: java -jar RePack.jar help", LoggingLevel.HELP);
        System.exit(1);
        return true;
    }

    private static void handleHelpArgument() {
        LOGGER.log("""

RePack usage, where we use $ as the alias for "java -jar RePack.jar"
Meaning: $ help -> is the same as -> java -jar RePack.jar help

Command Usages:
Show the help page:
    $ help

Compiling a pack to the optifine format:
    $ compile "path-in" "path-out"

    Example:
        $ compile "test-workspace" "test-out"
        The "test-workspace" folder will be compiled and the output folder will be called "test-out".""", LoggingLevel.HELP);
    }

    private static void handleCompileArgument(@NotNull final String... args) {
        final List<String> arguments = List.of(args).subList(1, args.length);
        if (arguments.size() != 2) {
            LOGGER.error("Expected 2 arguments, but got " + arguments.size() + ".");
            LOGGER.error("Usage: java -jar RePack.jar compile \"path-in\" \"path-out\"");
            System.exit(1);
            return;
        }
        final String in = arguments.get(0);
        final String out = arguments.get(1);

        final long startedMillis = System.currentTimeMillis();
        LOGGER.info(">>> Compiling workspace " + in + " to " + out + "...");
        compile(in, out);
        final long finishedMillis = System.currentTimeMillis();
        LOGGER.info("Finished in " + (finishedMillis - startedMillis) + "ms.");
    }

    private static void handleUnrecognizedArgument(@NotNull final String arg) {
        LOGGER.error("Unknown argument for RePack: '" + arg + "'. Use the 'help' argument to show the usage of RePack.");
        LOGGER.log("Usage: java -jar RePack.jar help", LoggingLevel.HELP);
    }

    public static void handle(@NotNull final String... args) {
        if (handleNoArguments(args)) return;
        final String arg = args[0];

        switch (arg) {
            case "help" -> handleHelpArgument();
            case "compile" -> handleCompileArgument(args);
            default -> handleUnrecognizedArgument(arg);
        }
    }

}