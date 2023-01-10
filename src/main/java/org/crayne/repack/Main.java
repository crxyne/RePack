package org.crayne.repack;

import org.crayne.repack.logging.Logger;
import org.crayne.repack.parsing.parser.Parser;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Main {

    public static void main(@NotNull final String... args) {
        final Logger logger = new Logger();
        final Parser parser = new Parser(logger);
        parser.parse(new File("example.rep"));

        parser.tree().ifPresent(System.out::println);
    }

}