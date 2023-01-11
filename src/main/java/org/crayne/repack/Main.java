package org.crayne.repack;

import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.parser.Parser;
import org.crayne.repack.parsing.parser.TreeAnalyzer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(@NotNull final String... args) throws IOException {
        final long startTime = System.currentTimeMillis();
        final Logger logger = new Logger();
        final Parser parser = new Parser(logger);

        final File f = new File("example.rep");

        final String code = Files.readString(f.toPath());
        final List<String> content = Arrays.stream(code.split("\n")).toList();

        parser.parse(f, code, content);
        final Optional<Node> tree = parser.tree();

        final boolean success = tree.isPresent() && new TreeAnalyzer(logger).analyze(tree.get(), content);
        if (!success) {
            logger.error("Could not parse pack.");
            return;
        }
        final long endTime = System.currentTimeMillis();
        logger.log("Successfully parsed pack in " + (endTime - startTime) + "ms.", LoggingLevel.SUCCESS);
    }

}